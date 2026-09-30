package discord.audioPlayer.players.lavalink.nodes;

import java.util.List;

public record NodeCandidate(String identifier, String host, int port, String password, boolean secure) {
    public static final List<NodeCandidate> CANDIDATES = List
            .of(new NodeCandidate("serentia", "lavalinkv4.serenetia.com", 80, "https://seretia.link/discord", false));
    // new NodeCandidate("millohost", "lava-v4.millohost.my.id", 443,
    // "https://discord.gg/mjS5J2K3ep", true));
}
