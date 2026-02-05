package discord.commands;

import discord.audio.PlayerManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Skip extends BaseCommand {

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        var guild = event.getGuild();
        var musicManager = PlayerManager.get().getGuildMusicManager(guild);

        if (musicManager.player.getPlayingTrack() == null) {
            event.getChannel().sendMessage("Nothing is playing!").queue();
            return;
        }

        musicManager.scheduler.nextTrack();
        event.getChannel().sendMessage("Skipped!").queue();
    }

}
