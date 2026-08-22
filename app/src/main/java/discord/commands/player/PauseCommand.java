package discord.commands.player;

import org.springframework.stereotype.Component;

import discord.commands.Command;
import discord.commands.CommandContext;

@Component
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
