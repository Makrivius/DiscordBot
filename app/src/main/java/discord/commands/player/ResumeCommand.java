package discord.commands.player;

import discord.commands.Command;
import discord.commands.CommandContext;

public class ResumeCommand implements Command {
    @Override
    public String name() {
        return "resume";
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.musicManager().resume();
    }
}
