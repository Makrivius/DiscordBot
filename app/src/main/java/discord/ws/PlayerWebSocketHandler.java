package discord.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import discord.audioPlayer.PlayerStateDTO;
import discord.audioPlayer.TrackScheduler;
import discord.commands.Command;
import discord.commands.CommandContext;
import discord.commands.CommandRegistry;
import discord.guild.SessionRegistry;
import discord.guild.VoiceConnector;
import discord.util.JsonUtil;
import net.dv8tion.jda.api.JDA;

@Component
public class PlayerWebSocketHandler extends TextWebSocketHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Gson gson = new Gson();

    private final SessionRegistry sessions;
    private final CommandRegistry registry;
    private final JDA jda;
    private final VoiceConnector voiceConnector;

    private final Map<String, Long> sessionToGuild = new ConcurrentHashMap<>();
    private final Map<String, Consumer<PlayerStateDTO>> listeners = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> pingTasks = new ConcurrentHashMap<>();

    private final ScheduledExecutorService pingScheduler = Executors.newSingleThreadScheduledExecutor();

    public PlayerWebSocketHandler(SessionRegistry sessions, CommandRegistry registry, JDA jda) {
        this.sessions = sessions;
        this.registry = registry;
        this.jda = jda;
        this.voiceConnector = new VoiceConnector(sessions);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String guildIdParam = param(session, "guildId");
        String userIdParam = param(session, "userId");

        if (guildIdParam == null) {
            closeQuietly(session);
            return;
        }

        long guildId = Long.parseLong(guildIdParam);
        sessionToGuild.put(session.getId(), guildId);

        TrackScheduler manager = sessions.getScheduler(guildId);
        Consumer<PlayerStateDTO> listener = state -> sendQuietly(session, gson.toJson(state));
        listeners.put(session.getId(), listener);
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
                    log.warn("Could not resolve a guild for activity connect", guildIdParam);
                }
            } catch (Exception e) {
                log.warn("Invalid userId param on activity connect: {}", userIdParam);
            }
        }

        sendQuietly(session, gson.toJson(manager.snapshot()));

        ScheduledFuture<?> pingTask = pingScheduler.scheduleAtFixedRate(() -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new PingMessage());
                }
            } catch (Exception e) {
                log.warn("Ping failed for guild {}", guildId, e);
            }
        }, 10, 10, TimeUnit.SECONDS);
        pingTasks.put(session.getId(), pingTask);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long guildId = sessionToGuild.get(session.getId());

        if (guildId == null) {
            return;
        }

        JsonObject json = gson.fromJson(message.getPayload(), JsonObject.class);
        String actionName = json.get("type").getAsString();
        Map<String, String> named = JsonUtil.toStringMap(json, "type");
        Command cmd = registry.get(actionName);
        if (cmd == null) {
            log.warn("Unknown command received: '{}' (guild {})", actionName, guildId);
            return;
        }
        CommandContext ctx = new CommandContext(guildId, sessions.getScheduler(guildId), sessions.getLoader(guildId),
                java.util.List.of(), named, msg -> {
                }, err -> {
                    log.error("WS command error: {}", err);
                }, payload -> {
                    sendQuietly(session, gson.toJson(Map.of("type", "searchResults", "results", payload)));
                });
        try {
            cmd.execute(ctx);
        } catch (Exception e) {
            log.error("Action failed", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long guildId = sessionToGuild.remove(session.getId());
        Consumer<PlayerStateDTO> listener = listeners.remove(session.getId());
        if (guildId != null && listener != null) {
            sessions.getScheduler(guildId).removeListener(listener);
        }
        ScheduledFuture<?> pingTask = pingTasks.remove(session.getId());
        if (pingTask != null) {
            pingTask.cancel(false);
        }
    }

    private String param(WebSocketSession session, String name) {
        var query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) {
                return java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private void sendQuietly(WebSocketSession session, String text) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (Exception e) {
            log.warn("Failed to send WS message", e);
        }
    }

    private void closeQuietly(WebSocketSession session) {
        try {
            session.close();
        } catch (Exception ignored) {
        }
    }
}
