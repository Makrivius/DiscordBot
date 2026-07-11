package discord.audioPlayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.lavalink.youtube.AllClientsFailedException;

public class TrackScheduler extends AudioEventAdapter {
    private static Logger log = LoggerFactory.getLogger(TrackScheduler.class);

    private final AudioPlayer player;
    private final AudioPlayerManagerHolder managerHolder;
    private final List<AudioTrack> queue = new ArrayList<>();
    private final Deque<AudioTrack> history = new ArrayDeque<>();
    private boolean shuffle = false;
    private String repeatMode = "off";
    private int currentIndex = -1;
    private Runnable onStateChange;

    public List<AudioTrack> getQueue() {
        return List.copyOf(queue);
    }

    public AudioTrack getCurrentTrack() {
        return player.getPlayingTrack();
    }

    public boolean isPaused() {
        return player.isPaused();
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public String getRepeatMode() {
        return repeatMode;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public TrackScheduler(AudioPlayer player, AudioPlayerManagerHolder managerHolder) {
        this.player = player;
        this.managerHolder = managerHolder;
    }

    public void setOnStateChanged(Runnable callback) {
        this.onStateChange = callback;
    }

    public void enqueue(AudioTrack track) {
        queue.add(track);
        if (player.getPlayingTrack() == null)
            playIndex(queue.size() - 1);
        else
            broadcast();
    }

    public void pause() {
        player.setPaused(true);
        broadcast();
    }

    public void resume() {
        player.setPaused(false);
        broadcast();
    }

    public void previous() {
        AudioTrack current = player.getPlayingTrack();
        if (current != null && current.getPosition() >= 3000) {
            current.setPosition(0);
            CompletableFuture.delayedExecutor(150, TimeUnit.MILLISECONDS).execute(this::broadcast);
            return;
        }

        if (queue.isEmpty())
            return;

        int prevIndex = currentIndex - 1;
        if (prevIndex < 0)
            prevIndex = 0;

        playIndex(prevIndex);
    }

    public void next() {
        advance();
    }

    public void toggleShuffle() {
        shuffle = !shuffle;
        broadcast();
    }

    public void cycleRepeat() {
        repeatMode = switch (repeatMode) {
            case "off" -> "track";
            case "track" -> "queue";
            default -> "off";
        };
        broadcast();
    }

    public void seek(long positionMs) {
        if (player.getPlayingTrack() != null) {
            player.getPlayingTrack().setPosition(positionMs);
            CompletableFuture.delayedExecutor(150, TimeUnit.MILLISECONDS).execute(this::broadcast);
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        CompletableFuture.delayedExecutor(3100, TimeUnit.MILLISECONDS).execute(this::broadcast);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason reason) {
        log.info("Track ended: {} | reason: {} | mayStartNext: {} | currentIndex: {}",
                track.getInfo().title, reason, reason.mayStartNext, currentIndex);
        if (!reason.mayStartNext)
            return;

        if (repeatMode.equals("track")) {
            playIndex(currentIndex);
            return;
        }

        advance();
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof AllClientsFailedException) {
            log.error("All YT clients are failed to load, hard refresh");
            managerHolder.refreshYoutubeSource();
            playIndex(currentIndex);
        }
    }

    private void playIndex(int i) {
        if (i < 0 || i >= queue.size())
            return;
        currentIndex = i;
        player.playTrack(queue.get(i).makeClone());
        broadcast();
    }

    private void advance() {
        int nextIndex = currentIndex + 1;
        if (nextIndex >= queue.size()) {
            if (repeatMode.equals("queue")) {
                playIndex(0);
            } else {
                currentIndex = -1;
                broadcast();
            }
            return;
        }
        playIndex(nextIndex);
    }

    private void broadcast() {
        if (onStateChange != null)
            onStateChange.run();
    }
}
