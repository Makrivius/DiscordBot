package discord.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;

import discord.guild.GuildMusicManager;
import discord.guild.SessionRegistry;

public class WsSessionManager {
    private final SessionRegistry sessions;
    private final Gson gson = new Gson();
    private final Map<WebSocket, Long> connectionToGuildMap = new ConcurrentHashMap<>();
    private final Map<WebSocket, Consumer<discord.audioPlayer.PlayerStateDTO>> activeListeners = new ConcurrentHashMap<>();

    public WsSessionManager(SessionRegistry sessions) {
        this.sessions = sessions;
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
        sessions.get(guildId).handleWsCommand(command); // add
    }
}
