package discord.commands.music;

import org.springframework.stereotype.Component;

import discord.audioPlayer.interfaces.TrackInterface;
import discord.commands.Command;
import discord.commands.CommandContext;

@Component
public class PlayNowCommand implements Command {
    @Override
    public String name() {
        return "playnow";
    }

    @Override
    public void execute(CommandContext ctx) {
        String query = ctx.named("trackId");
        if (query == null || query.isBlank()) {
            query = String.join(" ", ctx.positionArgs());
            if (!query.startsWith("http") || !query.contains(":"))
                query = "ytsearch:" + query;
        }
        if (query.isBlank()) {
            ctx.error("Usage: playnow <url|query>");
            return;
        }
        ctx.trackLoader().playNowById(query, obj -> {
            TrackInterface track = (TrackInterface) obj;
            ctx.reply("Playing now: **" + track.getTitle() + "**");
        }, ctx::error);
    }
}
