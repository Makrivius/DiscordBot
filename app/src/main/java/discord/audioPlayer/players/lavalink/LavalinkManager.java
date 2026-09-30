package discord.audioPlayer.players.lavalink;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.PlayerUpdateBuilder;
import discord.audioPlayer.players.lavalink.nodes.NodeHealthChecker;

public class LavalinkManager {
    private final LavalinkClient lavalinkClient;

    public LavalinkManager(String botToken) {
        long userId = Helpers.getUserIdFromToken(botToken);
        this.lavalinkClient = new LavalinkClient(userId);
        new NodeHealthChecker().registerAndWatch(lavalinkClient);
    }

    public Link getOrCreateLink(long guildId, LavalinkNode node) {
        return lavalinkClient.getOrCreateLink$lavalink_client(guildId, node);
    }

    public Link getOrCreateLink(long guildId) {
        return lavalinkClient.getOrCreateLink(guildId);
    }

    public LavalinkClient getLavalinkClient() {
        return lavalinkClient;
    }

    public PlayerUpdateBuilder createOrUpdatePlayer(Link link) {
        return link.createOrUpdatePlayer();
    }

    public LavalinkPlayer getCachedPlayer(Link link) {
        return link.getCachedPlayer();
    }

}
