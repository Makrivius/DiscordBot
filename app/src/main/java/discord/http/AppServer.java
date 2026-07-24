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
import discord.util.JsonUtil;
import io.javalin.Javalin;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;

public class AppServer {

    private final Logger log = LoggerFactory.getLogger(AppServer.class);

    private final Gson gson = new Gson();
    private final HttpClient http = HttpClient.newHttpClient();
    private final String clientId;
    private final String clientSecret;
    private final SessionRegistry sessions;
    private final CommandRegistry registry;

    private final Map<WsContext, Long> connToGuild = new ConcurrentHashMap<>();
    private final Map<WsContext, java.util.function.Consumer<discord.audioPlayer.PlayerStateDTO>> listeners = new ConcurrentHashMap<>();

    public AppServer(String clientId, String clientSecret, SessionRegistry sessions, CommandRegistry registry) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.sessions = sessions;
        this.registry = registry;
    }

    public void start(int port) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/dist";
                staticFiles.location = io.javalin.http.staticfiles.Location.CLASSPATH;
            });
        });

        app.post("/api/discord/token", this::handleTokenExchange);
        app.ws("/ws", ws -> {
            ws.onConnect(this::onOpen);
            ws.onMessage(this::onMessage);
            ws.onClose(this::onClose);
            ws.onError(ctx -> log.error("WS error: " + ctx.error()));
        });
        app.start(port);
    }

    private void handleTokenExchange(io.javalin.http.Context ctx) throws IOException, InterruptedException {
        JsonObject body = gson.fromJson(ctx.body(), JsonObject.class);
        String code = body.get("code").getAsString();

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
        if (guildIdParam == null) {
            ctx.session.close(4000, "Missing guildId");
            return;
        }
        long guildId = Long.parseLong(guildIdParam);
        connToGuild.put(ctx, guildId);

        GuildMusicManager manager = sessions.get(guildId);
        java.util.function.Consumer<discord.audioPlayer.PlayerStateDTO> listener = state -> ctx
                .send(gson.toJson(state));
        listeners.put(ctx, listener);
        manager.onStateChange(listener);

        ctx.send(gson.toJson(manager.snapshot()));
    }

    private void onMessage(WsMessageContext ctx) {
        Long guildId = connToGuild.get(ctx);
        if (guildId == null)
            return;

        JsonObject json = gson.fromJson(ctx.message(), JsonObject.class);
        String actionName = json.get("type").getAsString();
        Map<String, String> named = JsonUtil.toStringMap(json, "type");
        Command cmd = registry.get(actionName);
        if (cmd == null)
            return;

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
    }

}
