package discord.audioPlayer.players.lavalink;

import org.springframework.stereotype.Component;

import dev.arbjerg.lavalink.client.LavalinkClient;
import discord.audioPlayer.PlayerFactory;
import discord.audioPlayer.interfaces.PlayerInterface;

@Component
public class LavalinkPlayerFactory implements PlayerFactory {
    private final LavalinkClient client;

    public LavalinkPlayerFactory(LavalinkClient client) {
        this.client = client;
    }

    @Override
    public PlayerInterface create(long guildId) {
        return new LavalinkPlayerWrapper(client, client.getOrCreateLink(guildId));
    }
}