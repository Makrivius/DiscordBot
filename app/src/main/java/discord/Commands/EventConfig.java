package discord.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import tools.NullSafe;

public class EventConfig extends BaseCommand {

    @Override
    public String getName() {
        return "eventconfig";
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        var guild = event.getGuild(); // always non-null in guild messages
        var member = event.getMember(); // non-null in guild messages

        var guildId = NullSafe.get(guild, g -> g.getId());
        // TODO: Guild ID idenefier for DB queries and functionality
        if (member == null) {
            event.getChannel().sendMessage("This command can only be used in a server.").queue();
            return;
        }

        // Parse command and args
        var parsed = tools.CommandParser.parse(event.getMessage().getContentRaw());
        if (parsed == null) {
            return;
        }

        event.getChannel().sendMessage("Configured event settings.").queue();
    }
}
