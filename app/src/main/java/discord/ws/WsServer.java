package discord.ws;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord.guild.SessionRegistry;

public class WsServer extends WebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(WsServer.class);
    private final SessionRegistry sessions;
    private final Map<WebSocket, Long> connectionGuildMap = new ConcurrentHashMap<>();

    public WsServer(int port, SessionRegistry sessions) {
        super(new InetSocketAddress(port));
        this.sessions = sessions;
    }

@Override
public void onOpen(org.java_websocket.WebSocket conn, ClientHandshake handshake) {
    Long guildId = extractGuildId(conn.getResourceDescriptor());
    if(guildId == null){
        log.warn("Connection rejected - no guildId param");
        conn.close(4000, "Missing guildId");
        return;
    }
connectionGuildMap.put((WebSocket) conn, guildId);
log.info("WS client connected for guild {}", guildId);

sessions.get(guildId).a

}

    private Long extractGuildId(String resourceDescriptor) {
        try {
            URI uri = new URI(resourceDescriptor);
            String query = uri.getQuery();
            if (query == null)
                return null;
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2 && kv[0].equals("guildId")) {
                    return Long.parseLong(kv[1]);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse guild from {}", resourceDescriptor, e);
        }
        return null;
    }
}
