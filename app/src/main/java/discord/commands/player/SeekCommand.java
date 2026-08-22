package discord.commands.player;

import org.springframework.stereotype.Component;

import discord.commands.Command;
import discord.commands.CommandContext;

@Component
public class SeekCommand implements Command {
    @Override
    public String name() {
        return "seek";
    }

    @Override
    public void execute(CommandContext ctx) {
        String pos = ctx.named("positionMs");
        if (pos == null) {
            ctx.error("Missing positionMs");
            return;
        }
        ctx.musicManager().seek(Long.parseLong(pos));
    }
}
