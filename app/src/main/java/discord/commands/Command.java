package discord.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface Command {
    String name();

    void execute(MessageReceivedEvent event, String[] args);
}
