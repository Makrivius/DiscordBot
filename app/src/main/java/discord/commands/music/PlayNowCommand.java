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
        String trackId = ctx.named("trackId");
        if (trackId == null || trackId.isBlank()) {
            trackId = String.join(" ", ctx.positionArgs());
        }
        if (trackId.isBlank()) {
            ctx.error("Usage: playnow <url|query>");
            return;
        }
        ctx.trackLoader().playNowById(trackId, obj -> {
            TrackInterface track = (TrackInterface) obj;
            ctx.reply("Playing now: **" + track.getTitle() + "**");
        }, ctx::error);
    }
}
