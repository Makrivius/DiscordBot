package tools;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;

    private final List<AudioTrack> queue = new ArrayList<>();
    private final List<AudioTrack> history = new ArrayList<>();

    private AudioTrack currentTrack;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
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
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (track != null) {
                history.add(track);
            }
            nextTrack();
        }
    }
}
