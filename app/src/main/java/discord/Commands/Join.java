package discord.Commands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Join extends BaseCommand {

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        var guild = event.getGuild();          // always non-null in guild messages
        var member = event.getMember();        // non-null in guild messages
        if (member == null) {
            event.getChannel().sendMessage("This command can only be used in a server.").queue();
            return;
        }

        GuildVoiceState vs = member.getVoiceState();
        if (vs == null || !vs.inAudioChannel()) {
            event.getChannel().sendMessage("You must be in a voice channel!").queue();
            return;
        }

        var channel = vs.getChannel();         // the user's voice channel
        if (channel == null) {
            event.getChannel().sendMessage("Could not access your voice channel.").queue();
            return;
        }
        var audioManager = guild.getAudioManager();

        audioManager.openAudioConnection(channel);

        event.getChannel().sendMessage("Joined your voice channel.").queue();
    }
}
