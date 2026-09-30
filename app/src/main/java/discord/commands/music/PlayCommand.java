package discord.commands.music;

import java.util.List;

import org.springframework.stereotype.Component;

import discord.audioPlayer.interfaces.TrackInterface;
import discord.commands.Command;
import discord.commands.CommandContext;

@Component
public class PlayCommand implements Command {
    @Override
    public String name() {
        return "play";
    }

    @Override
    public void execute(CommandContext ctx) {
        if (ctx.positionArgs().isEmpty()) {
            ctx.error("Usage: play<query|url> [--shuffle]");
            return;
        }
        String query = String.join(" ", ctx.positionArgs());
        boolean shuffle = ctx.has("shuffle");
        ctx.trackLoader().loadAndQueue(query, shuffle, result -> ctx.reply(describeResult(result, shuffle)),
                error -> ctx.error(error.isEmpty() ? "Unknown error" : error));
    }

    private String describeResult(List<TrackInterface> result, boolean shuffle) {
        if (result.size() == 1) {
            return "Queued: **" + result.getFirst().getTitle() + "**";
        } else if (result.size() > 1) {
            return "Queued playlist: **" + "Ahahha ne sdelal" + "** (" + result.size() + " tracks"
                    + (shuffle ? ", shuffled" : "") + ")";
        }
        return "Queued.";
    }
}
