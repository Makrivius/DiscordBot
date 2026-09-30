package discord.commands.player;

import org.springframework.stereotype.Component;

import discord.commands.Command;
import discord.commands.CommandContext;

@Component
public class JumpCommand implements Command {
    @Override
    public String name() {
        return "jump";
    }

    @Override
    public void execute(CommandContext ctx) {
        try {
            int index = Integer.parseInt(ctx.named("index"));
            if (index < 0) {
                ctx.error("Jump index is below zero");
            }
            ctx.trackScheduler().jumpTo(index);
        } catch (Exception e) {
            ctx.error("Couldn't parse the jump index");
        }
    }
}
