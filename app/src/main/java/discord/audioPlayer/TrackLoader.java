package discord.audioPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import discord.audioPlayer.interfaces.PlayerInterface;
import discord.audioPlayer.interfaces.PlayerInterface.LoadStatus;
import discord.audioPlayer.interfaces.TrackInterface;

public class TrackLoader {
    private static final int MAX_LOAD_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 2000;

    private final TrackScheduler scheduler;
    private final PlayerInterface player;
    private final ScheduledExecutorService retryExecutor;

    public TrackLoader(TrackScheduler scheduler, PlayerInterface player, ScheduledExecutorService retryExecutor) {
        this.scheduler = scheduler;
        this.player = player;
        this.retryExecutor = retryExecutor;
    }

    public void enqueueById(String trackId, Consumer<TrackInterface> onSuccess, Consumer<String> onFail) {
        player.load(trackId).thenAccept(r -> {
            if (r.status() == LoadStatus.OK) {
                TrackInterface track = r.tracks().getFirst();
                scheduler.enqueue(track);
                scheduler.resume();
                onSuccess.accept(track);
            } else if (r.status() == LoadStatus.NO_MATCHES) {
                onFail.accept("Track not found: " + trackId);
            } else {
                onFail.accept("Load failed: " + r.error());
            }
        });
    }

    public void playNowById(String trackId, Consumer<Object> onSuccess, Consumer<String> onFail) {
        player.load(trackId).thenAccept(r -> {
            if (r.status() == LoadStatus.OK) {
                TrackInterface track = r.tracks().getFirst();
                scheduler.enqueue(track);
                scheduler.jumpTo(scheduler.getQueue().size() - 1);
                scheduler.resume();
                onSuccess.accept(track);
            } else if (r.status() == LoadStatus.NO_MATCHES) {
                onFail.accept("Track not found: " + trackId);
            } else {
                onFail.accept("Load failed: " + r.error());
            }
        });
    }

    public void loadAndQueue(String query, boolean shuffle, Consumer<List<TrackInterface>> onSuccess,
            Consumer<String> onFail) {
        attemptLoadAndQueue(query, shuffle, onSuccess, onFail, 0);
    }

    private void attemptLoadAndQueue(String query, boolean shuffle, Consumer<List<TrackInterface>> onSuccess,
            Consumer<String> onFail, int attempt) {
        player.load(query).thenAccept(r -> {
            switch (r.status()) {
            case OK -> {
                var t = r.tracks().getFirst();
                scheduler.enqueue(t);
                onSuccess.accept(List.of(t));
            }
            case PLAYLIST -> {
                var tracks = new ArrayList<>(r.tracks());
                if (shuffle)
                    Collections.shuffle(tracks);
                tracks.forEach(scheduler::enqueue);
                onSuccess.accept(tracks);
            }
            case NO_MATCHES -> onFail.accept("No matches found for: " + query);
            case FAILED -> {
                if (attempt < MAX_LOAD_RETRIES)
                    retryExecutor.schedule(() -> attemptLoadAndQueue(query, shuffle, onSuccess, onFail, attempt + 1),
                            RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
                else
                    onFail.accept("Failed to load: " + r.error());
            }
            }
        });
    }
}