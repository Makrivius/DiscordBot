package discord.commands.player;

import discord.commands.Command;
import discord.commands.CommandContext;

public class ClearQueueCommand implements Command {
    @Override
    public String name() {
        return "clearqueue";
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.musicManager().clearQueue();
    }
}
