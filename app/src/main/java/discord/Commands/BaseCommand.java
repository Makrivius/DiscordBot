package discord.Commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class BaseCommand {
    public abstract String getName();
    public abstract void execute(MessageReceivedEvent event);
}
