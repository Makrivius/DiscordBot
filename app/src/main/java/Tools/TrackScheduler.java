package tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;

    private final List<AudioTrack> queue = new ArrayList<>();
    private final List<AudioTrack> history = new ArrayList<>();

    private AudioTrack currentTrack;
    private TrackStartListener trackStartListener;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    public void setTrackStartListener(TrackStartListener listener) {
        this.trackStartListener = listener;
    }

    @FunctionalInterface
    public interface TrackStartListener {
        void onTrackStarted(AudioTrack track);
    }

    public void clearAll() {
        queue.clear();
        history.clear();
        currentTrack = null;
        player.stopTrack();
    }

    public void queue(AudioTrack track) {
        if (player.getPlayingTrack() == null) {
            currentTrack = track;
            player.startTrack(track, false);
        } else {
            queue.add(track);
        }
    }

    public void addPlaylist(List<AudioTrack> tracks, boolean shuffle) {
        if (shuffle) {
            Collections.shuffle(tracks);
        }
        for (AudioTrack track : tracks) {
            queue(track);
        }
    }

    public void togglePlayback() {
        player.setPaused(!player.isPaused());
    }

    public void stop() {
        clearAll();
    }

    public void previous() {
        if (history.isEmpty()) {
            return;
        }
        if (currentTrack != null) {
            queue.add(0, currentTrack);
        }
        AudioTrack previous = history.remove(history.size() - 1);
        currentTrack = previous;
        player.startTrack(previous, false);
    }

    public void skip() {
        if (currentTrack != null) {
            history.add(currentTrack);
        }
        nextTrack();
    }

    public void nextTrack() {
        if (queue.isEmpty()) {
            currentTrack = null;
            player.stopTrack();
            return;
        }

        currentTrack = queue.remove(0);
        player.startTrack(currentTrack, false);
    }

    public void shuffle() {
        Collections.shuffle(queue);
    }

    public List<AudioTrack> getQueue() {
        return queue;
    }

    public List<AudioTrack> getHistory() {
        return history;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        if (trackStartListener != null) {
            trackStartListener.onTrackStarted(track);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (track != null) {
                history.add(track);
            }
            nextTrack();
        }
    }
}
