package discord.ws;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import discord.commands.PlayerActionRegistry;
import discord.guild.GuildMusicManager;
import discord.guild.SessionRegistry;

public class WsSessionManager {
    private final SessionRegistry sessions;
    private final PlayerActionRegistry actions;
    private final Gson gson = new Gson();
    private final Map<WebSocket, Long> connectionToGuildMap = new ConcurrentHashMap<>();
    private final Map<WebSocket, Consumer<discord.audioPlayer.PlayerStateDTO>> activeListeners = new ConcurrentHashMap<>();

    public WsSessionManager(SessionRegistry sessions, PlayerActionRegistry actions) {
        this.sessions = sessions;
        this.actions = actions;
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

        Map<String, Object> params = new HashMap<>();
        json.entrySet().forEach(e -> {
            if (!e.getKey().equals("type"))
                params.put(e.getKey(), e.getValue().getAsString());
        });
        actions.dispatch(sessions.get(guildId), actionName, params);
    }
}
