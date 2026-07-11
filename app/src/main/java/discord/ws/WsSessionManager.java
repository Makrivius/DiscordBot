package discord.ws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import discord.commands.Command;
import discord.commands.CommandContext;
import discord.commands.CommandRegistry;
import discord.guild.GuildMusicManager;
import discord.guild.SessionRegistry;

public class WsSessionManager {
    private final Logger log = LoggerFactory.getLogger(WsSessionManager.class);

    private final SessionRegistry sessions;
    private final CommandRegistry registry;
    private final Gson gson = new Gson();
    private final Map<WebSocket, Long> connectionToGuildMap = new ConcurrentHashMap<>();
    private final Map<WebSocket, Consumer<discord.audioPlayer.PlayerStateDTO>> activeListeners = new ConcurrentHashMap<>();

    public WsSessionManager(SessionRegistry sessions, CommandRegistry registry) {
        this.sessions = sessions;
        this.registry = registry;
    }

    public void register(WebSocket conn, long guildId) {
        connectionToGuildMap.put(conn, guildId);
        GuildMusicManager manager = sessions.get(guildId);
        Consumer<discord.audioPlayer.PlayerStateDTO> listener = state -> conn.send(gson.toJson(state));

        activeListeners.put(conn, listener);
        manager.onStateChange(listener);

        conn.send(gson.toJson(manager.snapshot()));
    }

    public void unregister(WebSocket conn) {
        Long guildId = connectionToGuildMap.remove(conn);
        Consumer<discord.audioPlayer.PlayerStateDTO> listener = activeListeners.remove(conn);
        if (guildId != null && listener != null) {
            sessions.get(guildId).removeListener(listener);
        }
    }

    public void handleCommand(WebSocket conn, String command) {
        Long guildId = connectionToGuildMap.get(conn);
        if (guildId == null)
            return;

        JsonObject json = gson.fromJson(command, JsonObject.class);
        String actionName = json.get("type").getAsString();

        Map<String, String> named = new HashMap<>();
        json.entrySet().forEach(e -> {
            if (!e.getKey().equals("type"))
                named.put(e.getKey(), e.getValue().getAsString());
        });
        Command cmd = registry.get(actionName);
        if (cmd == null) {
            log.warn("Unknown player action: {}", actionName);
            return;
        }
        CommandContext ctx = new CommandContext(guildId, sessions.get(guildId), List.of(), named,
                msg -> {
                }, err -> log.warn("WS command '{}' failed: {}", actionName, err),
                payload -> conn.send(gson.toJson(Map.of("type", "searchResults", "results", payload))));
        try {
            cmd.execute(ctx);
        } catch (Exception e) {
            log.error("Action '{}' failed", actionName, e);
        }
    }
}
