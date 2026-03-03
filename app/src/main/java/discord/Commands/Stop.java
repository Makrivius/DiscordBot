package discord.commands;

import discord.util.CommandParser.ParsedCommand;

public class Stop extends BaseCommand {

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Stops playback and clears the queue.";
    }

    @Override
    public String getUsage() {
        return "!stop";
    }

    @Override
    public void execute(net.dv8tion.jda.api.events.message.MessageReceivedEvent event, ParsedCommand cmd) {
        var guild = event.getGuild();
        var musicManager = discord.audio.PlayerManager.get().getGuildMusicManager(guild);

        if (musicManager.player.getPlayingTrack() == null) {
            event.getChannel().sendMessage("Nothing is playing!").queue();
            return;
        }

        musicManager.scheduler.clearAll();
        event.getChannel().sendMessage("Stopped!").queue();
    }
}
