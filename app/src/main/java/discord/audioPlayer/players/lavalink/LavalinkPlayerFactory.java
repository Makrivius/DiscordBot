package discord.audioPlayer.players.lavalink;

import org.springframework.stereotype.Component;

import discord.audioPlayer.PlayerFactory;
import discord.audioPlayer.interfaces.PlayerInterface;

@Component
public class LavalinkPlayerFactory implements PlayerFactory {
    private final LavalinkManager manager;

    public LavalinkPlayerFactory(LavalinkManager manager) {
        this.manager = manager;
    }

    @Override
    public PlayerInterface create(long guildId) {
        return new LavalinkPlayerWrapper(manager, manager.getOrCreateLink(guildId));
    }
}