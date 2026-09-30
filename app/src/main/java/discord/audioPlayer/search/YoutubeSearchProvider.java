package discord.audioPlayer.search;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

@Component
public class YoutubeSearchProvider implements SearchProvider {
    private static final Logger log = LoggerFactory.getLogger(YoutubeSearchProvider.class);
    private static final int MAX_RESULTS = 10;

    private final AudioPlayerManager playerManager;

    public YoutubeSearchProvider(AudioPlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public CompletableFuture<List<SearchHit>> search(String query) {
        var future = new CompletableFuture<List<SearchHit>>();
        String lookup = query.startsWith("http") ? query : "ytsearch:" + query;

        playerManager.loadItemOrdered(this, lookup, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                future.complete(List.of(toHit(track)));
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                future.complete(
                        playlist.getTracks().stream().limit(MAX_RESULTS).map(YoutubeSearchProvider::toHit).toList());
            }

            @Override
            public void noMatches() {
                future.complete(List.of());
            }

            @Override
            public void loadFailed(FriendlyException e) {
                log.error("YouTube search failed for '{}'", query, e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private static SearchHit toHit(AudioTrack t) {
        var info = t.getInfo();
        String id = info.identifier;
        String url = info.uri != null ? info.uri : "https://www.youtube.com/watch?v=" + id;
        String thumb = info.artworkUrl != null ? info.artworkUrl : "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg";
        return new SearchHit(id, url, info.title, info.author, info.length, thumb);
    }
}