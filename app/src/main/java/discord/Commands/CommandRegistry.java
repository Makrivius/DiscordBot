package discord.commands;

import discord.Config;
import discord.util.CommandParser;
import discord.util.CommandParser.ParsedCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.Map;

public class CommandRegistry extends ListenerAdapter {

    private final Map<String, BaseCommand> commands;

    public CommandRegistry(Map<String, BaseCommand> commands) {
        this.commands = commands;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String msg = event.getMessage().getContentRaw();
        if (!msg.startsWith(Config.PREFIX)) return;

        ParsedCommand parsed = CommandParser.parse(msg);
        if (parsed == null) return;

        BaseCommand cmd = commands.get(parsed.command);
        if (cmd == null) return;

        cmd.execute(event, parsed);
    }
}