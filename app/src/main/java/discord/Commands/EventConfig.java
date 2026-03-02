package discord.commands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EventConfig extends BaseCommand {

    @Override
    public String getName() {
        return "eventconfig";
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        var guild = event.getGuild(); // always non-null in guild messages
        var member = event.getMember(); // non-null in guild messages
        if (member == null) {
            event.getChannel().sendMessage("This command can only be used in a server.").queue();
            return;
        }
        
        event.getChannel().sendMessage("Joined your voice channel.").queue();
    }
}
