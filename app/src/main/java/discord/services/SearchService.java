package discord.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import discord.audioPlayer.search.SearchHit;
import discord.audioPlayer.search.SearchProvider;

@Service
public class SearchService {
    private final List<SearchProvider> providers;

    public SearchService(List<SearchProvider> providers) {
        this.providers = providers;
    }

    public CompletableFuture<List<SearchHit>> search(String query) {
        var futures = providers.stream().map(p -> p.search(query).exceptionally(e -> List.of())).toList();

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> futures.stream().flatMap(f -> f.join().stream()).toList());
    }
}
