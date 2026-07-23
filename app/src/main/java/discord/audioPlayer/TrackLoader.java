package discord.audioPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class TrackLoader {
    private final Logger log = LoggerFactory.getLogger(TrackLoader.class);
    private final AudioPlayerManager playerManager;
    private final TrackScheduler scheduler;

    public TrackLoader(AudioPlayerManager playerManager, TrackScheduler scheduler) {
        this.playerManager = playerManager;
        this.scheduler = scheduler;
    }

    public void loadAndQueue(String query, boolean shuffle, Consumer<AudioTrack> onSuccess, Consumer<String> onFail) {
        String lookup = normalizeQuery(query);

        playerManager.loadItemOrdered(this, lookup, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                scheduler.enqueue(track);
                onSuccess.accept(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult()) {
                    AudioTrack top = playlist.getTracks().getFirst();
                    scheduler.enqueue(top);
                    onSuccess.accept(top);
                    return;
                }
                List<AudioTrack> tracks = new ArrayList<>(playlist.getTracks());
                if (shuffle)
                    Collections.shuffle(tracks);
                tracks.forEach(scheduler::enqueue);
                onSuccess.accept(tracks.getFirst());
            }

            @Override
            public void noMatches() {
                log.warn("No Matches for query {}", query);
                onFail.accept("No matches found for : " + query);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                log.error("Load failed for query '{}'", query, exception);
                onFail.accept("Failed to load: " + exception.getMessage());
            }
        });
        scheduler.resume();
    }

    public void search(String query, Consumer<List<AudioTrack>> onResults, Consumer<String> onFail) {
        String lookup = normalizeQuery(query);

        playerManager.loadItemOrdered(this, lookup, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                onResults.accept(List.of(track));
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                onResults.accept(new ArrayList<>(playlist.getTracks()));
            }

            @Override
            public void noMatches() {
                log.warn("No matches for search query {}", query);
                onFail.accept("No matches found for: " + query);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                log.error("Search failed for query '{}'", query, exception);
                onFail.accept("Search failed: " + exception.getMessage());
            }
        });
    }

    private String normalizeQuery(String query) {
        if (query.startsWith("http") && query.contains("list=") && !query.contains("playlist?list=")) {
            return query.replaceAll("[&?]list=[^&]+", "");
        }
        return query.startsWith("http") ? query : "ytsearch:" + query;
    }

}
