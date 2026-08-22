package discord.commands.player;

import org.springframework.stereotype.Component;

import discord.commands.Command;
import discord.commands.CommandContext;

@Component
public class ResumeCommand implements Command {
    @Override
    public String name() {
        return "resume";
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.musicManager().resume();
    }
}
