package discord.commands.music;

import discord.commands.Command;
import discord.commands.CommandContext;

public class PlayNowCommand implements Command {
    @Override
    public String name() {
        return "playnow";
    }

    @Override
    public void execute(CommandContext ctx) {
        String trackId = ctx.named("trackId");
        if (trackId == null || trackId.isBlank()) {
            ctx.error("Missing trackId");
            return;
        }
        ctx.musicManager().playNowById(trackId, track -> ctx.reply("Playing now: **" + track.getInfo().title + "**"),
                error -> ctx.error(error));
    }
}
