package discord.commands.music;

import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

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
        ctx.musicManager().loadAndQueue(query, shuffle, result -> ctx.reply(describeResult(result, shuffle)),
                error -> ctx.error(error.isEmpty() ? "Unknown error" : error));
    }

    private String describeResult(Object result, boolean shuffle) {
        if (result instanceof AudioTrack track) {
            return "Queued: **" + track.getInfo().title + "**";
        } else if (result instanceof AudioPlaylist playlist) {
            return "Queued playlist: **" + playlist.getName() + "** ("
                    + playlist.getTracks().size() + " tracks" + (shuffle ? ", shuffled" : "") + ")";
        }
        return "Queued.";
    }
}
