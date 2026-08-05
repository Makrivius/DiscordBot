package discord.audioPlayer.nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.ReadyEvent;
import dev.arbjerg.lavalink.client.event.WebSocketClosedEvent;

public class NodeHealthChecker {
    private static final Logger log = LoggerFactory.getLogger(NodeHealthChecker.class);

    public void registerAndWatch(LavalinkClient client) {
        client.on(ReadyEvent.class).subscribe(event -> log.info("Node '{}' connected (resumed={},sessionId={})",
                event.getNode().getName(), event.getResumed(), event.getSessionId()));

        client.on(WebSocketClosedEvent.class).subscribe(event -> log.warn(
                "Node '{}' websocket closed for guild {} (code={}, reason={}, byRemote={})",
                event.getNode().getName(), event.getGuildId(), event.getCode(), event.getCode(), event.getReason(),
                event.getByRemote()));

        for (NodeCandidate c : NodeCandidate.CANDIDATES) {
            try {
                String scheme = c.secure() ? "https" : "http";
                NodeOptions options = new NodeOptions.Builder()
                        .setName(c.identifier())
                        .setServerUri(scheme + "://" + c.host() + ":" + c.port())
                        .setPassword(c.password())
                        .build();
                client.addNode(options);
                log.info("Added candidate node '{}' ({}:{}), waiting for ReadyEvent...",
                        c.identifier(), c.host(), c.port());
            } catch (Exception e) {
                log.warn("Failed to add candidate node '{}': {}", c.identifier(), e.getMessage());
            }
        }
    }
}
