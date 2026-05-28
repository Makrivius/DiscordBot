package discord.commands;

public class Leave extends BaseCommand {

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Leaves the voice channel.";
    }

    @Override
    public String getUsage() {
        return "!leave";
    }

    @Override
    public void execute(net.dv8tion.jda.api.events.message.MessageReceivedEvent event,
            discord.util.CommandParser.ParsedCommand cmd) {
        var guild = event.getGuild();
        var audioManager = guild.getAudioManager();

        if (!audioManager.isConnected()) {
            event.getChannel().sendMessage("I'm not connected to a voice channel!").queue();
            return;
        }

        audioManager.closeAudioConnection();
        event.getChannel().sendMessage("Left the voice channel!").queue();
    }

}
