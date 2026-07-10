package discord.commands.player;

import discord.commands.Command;
import discord.commands.CommandContext;

public class NextCommand implements Command {
    @Override
    public String name() {
        return "next";
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.musicManager().next();
    }
}
