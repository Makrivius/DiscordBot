package discord.commands.player;

import discord.commands.Command;
import discord.commands.CommandContext;

public class PreviousCommand implements Command {
    @Override
    public String name() {
        return "prev";
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.musicManager().previous();
    }
}
