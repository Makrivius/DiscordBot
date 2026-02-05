package discord.handlers;

import java.util.Map;

import javax.annotation.Nonnull;

import discord.commands.BaseCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

    private final Map<String, BaseCommand> commands;

    public CommandListener(Map<String, BaseCommand> commands) {
        this.commands = commands;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();

        if (!msg.startsWith("!")) return;

        String name = msg.substring(1).split(" ")[0];

        BaseCommand cmd = commands.get(name);
        if (cmd != null) {
            cmd.execute(event);
        }
    }
}

