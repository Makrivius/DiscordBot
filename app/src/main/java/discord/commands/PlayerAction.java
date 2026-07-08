package discord.commands;

import java.util.Map;

import discord.guild.GuildMusicManager;

@FunctionalInterface
public interface PlayerAction {
    void execute(GuildMusicManager manager, Map<String, Object> args);
}
