package discord.audioPlayer;

import discord.audioPlayer.interfaces.PlayerInterface;

public interface PlayerFactory {
    PlayerInterface create(long guildId);
}