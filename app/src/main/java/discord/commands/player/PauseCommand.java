package discord.commands.player;

import discord.commands.Command;
import discord.commands.CommandContext;

public class PauseCommand implements Command {
    @Override
    public String name() {
        return "pause";
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.musicManager().pause();
    }
}
