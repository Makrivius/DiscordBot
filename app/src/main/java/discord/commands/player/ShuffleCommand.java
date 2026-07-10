package discord.commands.player;

import discord.commands.Command;
import discord.commands.CommandContext;

public class ShuffleCommand implements Command {
    @Override
    public String name() {
        return "shuffle";
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.musicManager().toggleShuffle();
    }
}
