package discord.ws;

import java.net.InetSocketAddress;
import java.net.URI;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsServer extends WebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(WsServer.class);
    private final WsSessionManager sessionManager;

    public WsServer(int port, WsSessionManager sessionManager) {
        super(new InetSocketAddress(port));
        this.sessionManager = sessionManager;
    }

    @Override
    public void onOpen(org.java_websocket.WebSocket conn, ClientHandshake handshake) {
        Long guildId = extractGuildId(conn.getResourceDescriptor());
        if (guildId == null) {
            conn.close(4000, "Missing guildId");
            return;
        }
        sessionManager.register(conn, guildId);
        log.info("Ws client connected for guild {}", guildId);

    }

    @Override
    public void onMessage(org.java_websocket.WebSocket conn, String message) {
        sessionManager.handleCommand(conn, message);
    }

    @Override
    public void onClose(org.java_websocket.WebSocket conn, int code, String reason, boolean remote) {
        sessionManager.unregister(conn);
        log.info("Ws client disconnected code: {} reason: {}", code, reason);
    }

    @Override
    public void onError(org.java_websocket.WebSocket conn, Exception ex) {
        log.error("Ws error", ex);
    }

    @Override
    public void onStart() {
        log.info("Ws server started");
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
