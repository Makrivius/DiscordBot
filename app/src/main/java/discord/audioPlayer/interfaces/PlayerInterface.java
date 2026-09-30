package discord.audioPlayer.interfaces;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PlayerInterface {
    public enum PlayResult {
        STARTED, RETRY, FAILED
    }

    enum LoadStatus {
        OK, PLAYLIST, NO_MATCHES, FAILED
    }

    // Player
    TrackInterface getCurrentTrack();

    void setPosition(long position);

    long getCurrentPosition();

    boolean isPaused();

    CompletableFuture<PlayResult> requestPlay(TrackInterface track);

    void resume();

    void pause();

    void setBroadcastFunction(Runnable broadcastFunction);

    void setOnTrackEnd(Runnable onTrackEndReady);

    // Load
    record LoadResult(LoadStatus status, List<TrackInterface> tracks, String error) {
    }

    CompletableFuture<LoadResult> load(String query);
}
