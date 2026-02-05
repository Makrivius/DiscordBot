package discord.commands;

public class TogglePlayback extends BaseCommand {

    @Override
    public String getName() {
        return "toggle";
    }

    @Override
    public void execute(net.dv8tion.jda.api.events.message.MessageReceivedEvent event) {
        var guild = event.getGuild();
        var musicManager = discord.audio.PlayerManager.get().getGuildMusicManager(guild);

        if (musicManager.player.getPlayingTrack() == null) {
            event.getChannel().sendMessage("Nothing is playing!").queue();
            return;
        }

        musicManager.scheduler.togglePlayback();
        event.getChannel().sendMessage("Toggled playback!").queue();
    }

}
