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

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkLoadResult;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.NoMatches;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.SearchResult;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.player.TrackLoaded;

public class TrackLoader {
    private final Logger log = LoggerFactory.getLogger(TrackLoader.class);
    private final AudioPlayerManager playerManager;
    private final Link link;
    private final TrackScheduler scheduler;

    public TrackLoader(AudioPlayerManager playerManager, Link link, TrackScheduler scheduler) {
        this.playerManager = playerManager;
        this.link = link;
        this.scheduler = scheduler;
    }

    public void enqueueById(String trackId, Consumer<Track> onSuccess, Consumer<String> onFail) {
        link.loadItem(trackId).subscribe(result -> onSingleTrackLoaded(result, trackId, onFail, track -> {
            scheduler.enqueue(track);
            onSuccess.accept(track);
        }),
                err -> {
                    log.error("loadItem failed for '{}' on node '{}'", trackId,
                            link.getNode() != null ? link.getNode().getName() : "unknown", err);
                    onFail.accept("Load failed: " + err.getMessage());
                });
        scheduler.resume();
    }

    public void playNowById(String trackId, Consumer<Track> onSuccess, Consumer<String> onFail) {
        link.loadItem(trackId).subscribe(result -> onSingleTrackLoaded(result, trackId, onFail, track -> {
            scheduler.enqueue(track);
            scheduler.jumpTo(scheduler.getQueue().size() - 1);
            onSuccess.accept(track);
        }),
                err -> {
                    log.error("loadItem failed for '{}' on node '{}'", trackId,
                            link.getNode() != null ? link.getNode().getName() : "unknown", err);
                    onFail.accept("Load failed: " + err.getMessage());
                });
        scheduler.resume();
    }

    public void loadAndQueue(String query, boolean shuffle, Consumer<Track> onSuccess, Consumer<String> onFail) {
        String lookup = normalizeQuery(query);

        link.loadItem(lookup).subscribe(result -> {
            switch (result) {
                case TrackLoaded loaded -> {
                    Track track = loaded.getTrack();
                    scheduler.enqueue(track);
                    onSuccess.accept(track);
                }
                case PlaylistLoaded playlistLoaded -> {
                    List<Track> tracks = new ArrayList<>(playlistLoaded.getTracks());
                    if (shuffle)
                        Collections.shuffle(tracks);
                    tracks.forEach(scheduler::enqueue);
                    onSuccess.accept(tracks.get(0));
                }
                case SearchResult searchResult -> {
                    Track top = searchResult.getTracks().getFirst();
                    scheduler.enqueue(top);
                    onSuccess.accept(top);
                }
                case NoMatches _ -> {
                    log.warn("No matches for query {}", query);
                    onFail.accept("No matches found for: " + query);
                }
                case LoadFailed failed -> {
                    log.error("Load failed due to: {}", failed.getException().getMessage());
                    onFail.accept("Failed to load: " + failed.getException().getMessage());
                }
                default -> onFail.accept("Unexpected result for: " + query);
            }
        }, err -> {
            log.error("Load failed for query '{}'", query, err);
            onFail.accept("Failed to load: " + err.getMessage());
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

    private void onSingleTrackLoaded(LavalinkLoadResult result, String context, Consumer<String> onFail,
            Consumer<Track> onTrack) {
        switch (result) {
            case TrackLoaded loaded -> onTrack.accept(loaded.getTrack());
            case NoMatches _ -> onFail.accept("Track not found: " + context);
            case LoadFailed failed -> onFail.accept("Load failed due to: " + failed.getException().getMessage());
            default -> onFail.accept("Unexpected result for: " + context);
        }
    }
}
