package discord.commands.player;

import org.springframework.stereotype.Component;

import discord.commands.Command;
import discord.commands.CommandContext;

@Component
public class RemoveCommand implements Command {
    @Override
    public String name() {
        return "remove";
    }

    @Override
    public void execute(CommandContext ctx) {
        String queueId = ctx.named("queueId");
        if (queueId == null || queueId.isBlank()) {
            ctx.error("Missing queueId");
            return;
        }
        ctx.musicManager().remove(queueId);
    }
}
