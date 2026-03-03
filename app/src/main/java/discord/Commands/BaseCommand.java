package discord.commands;

import discord.util.CommandParser;
import discord.util.CommandParser.ParsedCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class BaseCommand {

    public abstract String getName();
    public abstract String getDescription();  // short, shown in !help list
    public abstract String getUsage();        // full usage, shown in !help <cmd>

    public abstract void execute(MessageReceivedEvent event, ParsedCommand cmd);

    /** Called by your command router — parses once, then delegates. */
    public void handle(MessageReceivedEvent event) {
        ParsedCommand cmd = CommandParser.parse(event.getMessage().getContentRaw());
        if (cmd == null) return;
        execute(event, cmd);
    }
}