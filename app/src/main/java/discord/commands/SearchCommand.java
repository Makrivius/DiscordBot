package discord.commands;

import org.springframework.stereotype.Component;

import discord.audioPlayer.PlayerStateDTO.SearchResultDTO;
import discord.audioPlayer.search.SearchHit;
import discord.services.SearchService;
import discord.util.ThumbnailUtil;

@Component
public class SearchCommand implements Command {
    private final SearchService searchService;

    public SearchCommand(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public String name() {
        return "search";
    }

    @Override
    public void execute(CommandContext ctx) {
        String query = ctx.named("query");
        if (query == null || query.isBlank()) {
            query = String.join(" ", ctx.positionArgs());
        }
        if (query.isBlank()) {
            ctx.error("Missing query");
            return;
        }

        searchService.search(query).thenAccept(hits -> ctx.replyData(hits.stream().map(this::toDto).toList()))
                .exceptionally(e -> {
                    ctx.error("Search failed");
                    return null;
                });
    }

    private SearchResultDTO toDto(SearchHit h) {
        return new SearchResultDTO(h.id(), h.title(), h.author(), h.durationMs(), ThumbnailUtil.getThumbnails(h.id()));
    }
}