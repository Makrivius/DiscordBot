package discord.commands;

import org.springframework.stereotype.Component;

@Component
public class PingCommand implements Command {
    @Override
    public String name() {
        return "ping";
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.reply("Pong!");
        ;
    }
}
