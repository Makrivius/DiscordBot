package discord.commands.player;

import org.springframework.stereotype.Component;

import discord.commands.Command;
import discord.commands.CommandContext;

@Component
public class ClearQueueCommand implements Command {
    @Override
    public String name() {
        return "clearqueue";
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.trackScheduler().clearExceptCurrent();
    }
}
