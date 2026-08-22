package discord.commands.player;

import org.springframework.stereotype.Component;

import discord.commands.Command;
import discord.commands.CommandContext;

@Component
public class RepeatCommand implements Command {
    @Override
    public String name() {
        return "repeat";
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.musicManager().cycleRepeat();
    }
}
