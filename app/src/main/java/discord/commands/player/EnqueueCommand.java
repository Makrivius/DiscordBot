package discord.commands.player;

import discord.commands.Command;
import discord.commands.CommandContext;

public class EnqueueCommand implements Command {
    @Override
    public String name() {
        return "enqueue";
    }

    @Override
    public void execute(CommandContext ctx) {
        String trackId = ctx.named("trackId");
        if (trackId == null || trackId.isBlank()) {
            ctx.error("Missing trackId");
            return;
        }
        ctx.musicManager().enqueueById(trackId, track -> ctx.reply("Queued: **" + track.getInfo().title + "**"),
                error -> ctx.error(error));
    }
}
