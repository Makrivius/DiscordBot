package discord.commands;

import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import discord.audioPlayer.PlayerStateDTO.SearchResultDTO;
import discord.util.ThumbnailUtil;

@Component
public class SearchCommand implements Command {
    @Override
    public String name() {
        return "search";
    }

    @Override
    public void execute(CommandContext ctx) {
        String query = ctx.named("query");
        String requestId = ctx.named("requestId");
        if (query == null || query.isBlank() || requestId.isBlank()) {
            ctx.error("Missing query or requestId");
            return;
        }
        ctx.musicManager().search(query, results -> ctx.replyData(results.stream().map(this::toDto).toList()),
                ctx::error);
    }

    private SearchResultDTO toDto(AudioTrack track) {
        AudioTrackInfo info = track.getInfo();
        return new SearchResultDTO(track.getIdentifier(), info.title, info.author, info.length,
                ThumbnailUtil.getThumbnails(track.getIdentifier()));
    }

}
