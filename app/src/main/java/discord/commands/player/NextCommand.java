package discord.commands.player;

import org.springframework.stereotype.Component;

import discord.commands.Command;
import discord.commands.CommandContext;

@Component
public class NextCommand implements Command {
    @Override
    public String name() {
        return "next";
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.trackScheduler().next();
    }
}
