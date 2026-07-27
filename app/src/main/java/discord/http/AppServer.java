package discord.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import discord.commands.Command;
import discord.commands.CommandContext;
import discord.commands.CommandRegistry;
import discord.guild.GuildMusicManager;
import discord.guild.SessionRegistry;
import discord.guild.VoiceConnector;
import discord.util.JsonUtil;
import io.javalin.Javalin;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import net.dv8tion.jda.api.JDA;

public class AppServer {

    private final Logger log = LoggerFactory.getLogger(AppServer.class);

    private final Gson gson = new Gson();
    private final HttpClient http = HttpClient.newHttpClient();
    private final String clientId;
    private final String clientSecret;
    private final SessionRegistry sessions;
    private final CommandRegistry registry;
    private final JDA jda;
    private final VoiceConnector voiceConnector;

    private final Map<WsContext, Long> connToGuild = new ConcurrentHashMap<>();
    private final Map<WsContext, java.util.function.Consumer<discord.audioPlayer.PlayerStateDTO>> listeners = new ConcurrentHashMap<>();

    private final Map<WsContext, java.util.concurrent.ScheduledFuture<?>> pingTasks = new ConcurrentHashMap<>();
    private final java.util.concurrent.ScheduledExecutorService pingScheduler = java.util.concurrent.Executors
            .newSingleThreadScheduledExecutor();

    public AppServer(String clientId, String clientSecret, SessionRegistry sessions, CommandRegistry registry,
            JDA jda) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.sessions = sessions;
        this.registry = registry;
        this.jda = jda;
        this.voiceConnector = new VoiceConnector(sessions);
    }

    public void start(int port) {
        Javalin app = Javalin.create(config -> {
            config.jetty.modifyWebSocketServletFactory(factory -> {
                factory.setIdleTimeout(java.time.Duration.ofSeconds(60));
            });

            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/dist";
                staticFiles.location = io.javalin.http.staticfiles.Location.CLASSPATH;

            });

            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/.proxy";
                staticFiles.directory = "/dist";
                staticFiles.location = io.javalin.http.staticfiles.Location.CLASSPATH;

            });

            config.routes.post("/api/discord/token", this::handleTokenExchange);

            config.routes.ws("/ws", ws -> {
                ws.onConnect(this::onOpen);
                ws.onMessage(this::onMessage);
                ws.onClose(this::onClose);
                ws.onError(ctx -> log.error("WS error: " + ctx.error()));
            });
        });

        app.start(port);
    }

    private void handleTokenExchange(io.javalin.http.Context ctx) throws IOException, InterruptedException {
        JsonObject body = gson.fromJson(ctx.body(), JsonObject.class);
        String code = body.get("code").getAsString();

        String redirectUri = ctx.scheme() + "://" + ctx.host();
        log.info("Dynamic Redirect URI generated: {}", redirectUri);

        String form = "client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&grant_type=authorization_code"
                + "&code=" + code;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://discord.com/api/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject discordResponse = gson.fromJson(response.body(), JsonObject.class);

        ctx.json(Map.of("access_token", discordResponse.get("access_token").getAsString()));
    }

    private void onOpen(WsConnectContext ctx) {
        String guildIdParam = ctx.queryParam("guildId");
        String userIdParam = ctx.queryParam("userId");
        if (guildIdParam == null) {
            ctx.session.close();
            return;
        }
        long guildId = Long.parseLong(guildIdParam);
        connToGuild.put(ctx, guildId);

        GuildMusicManager manager = sessions.get(guildId);
        java.util.function.Consumer<discord.audioPlayer.PlayerStateDTO> listener = state -> ctx
                .send(gson.toJson(state));
        listeners.put(ctx, listener);
        manager.onStateChange(listener);

        if (userIdParam != null) {
            try {
                long userId = Long.parseLong(userIdParam);
                var guild = jda.getGuildById(guildId);
                if (guild != null) {
                    boolean connected = voiceConnector.ensureConnected(guild, userId);
                    if (!connected) {
                        log.warn("Activity user {} isn't in a voice channel in guild {}", userId, guildId);
                    }
                } else {
                    log.warn("Could not resolve guild {} for activity connect", userIdParam);
                }
            } catch (Exception e) {
                log.warn("Invalid userId param on activity connect: {}", userIdParam);
            }
        }

        ctx.send(gson.toJson(manager.snapshot()));

        var pingTask = pingScheduler.scheduleAtFixedRate(() -> {
            try {
                if (ctx.session.isOpen()) {
                    ctx.session.sendPing(java.nio.ByteBuffer.allocate(0),
                            org.eclipse.jetty.websocket.api.Callback.NOOP);
                }
            } catch (Exception e) {
                log.warn("Ping filed for guild {}", guildId, e);
            }
        }, 10, 10, java.util.concurrent.TimeUnit.SECONDS);
        pingTasks.put(ctx, pingTask);
    }

    private void onMessage(WsMessageContext ctx) {
        Long guildId = connToGuild.get(ctx);
        if (guildId == null)
            return;

        JsonObject json = gson.fromJson(ctx.message(), JsonObject.class);
        String actionName = json.get("type").getAsString();
        Map<String, String> named = JsonUtil.toStringMap(json, "type");
        Command cmd = registry.get(actionName);
        if (cmd == null) {
            log.warn("Unknown command received: '{}' (guild {})", actionName, guildId);
            return;
        }

        CommandContext cctx = new CommandContext(guildId, sessions.get(guildId), java.util.List.of(), named, msg -> {
        }, err -> log.error("WS command failed: " + err),
                payload -> ctx.send(gson.toJson(Map.of("type", "searchResults", "results", payload))));
        try {
            cmd.execute(cctx);
        } catch (Exception e) {
            log.error("Action failed: " + e);
        }
    }

    private void onClose(WsCloseContext ctx) {
        Long guildId = connToGuild.remove(ctx);
        var listener = listeners.remove(ctx);
        if (guildId != null && listener != null) {
            sessions.get(guildId).removeListener(listener);
        }
        var pingTask = pingTasks.remove(ctx);
        if (pingTask != null)
            pingTask.cancel(false);
    }

}
