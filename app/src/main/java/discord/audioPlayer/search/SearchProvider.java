package discord.audioPlayer.search;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SearchProvider {
    CompletableFuture<List<SearchHit>> search(String query);
}
