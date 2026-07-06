package discord.AudioPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final List<AudioTrack> queue = new ArrayList<>();
    private int currentIndex = -1;
    private Consumer<PlayerStateDTO> onStateChange;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    public void enqueue(AudioTrack track) {
        queue.add(track);
        if (currentIndex == -1)
            plaIndex(0);
        broadcast();
    }

    private void plaIndex(int i) {
        if (i < 0 || i >= queue.size())
            return;
        currentIndex = i;
        player.playTrack(queue.get(i));
        broadcast();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason reason) {
        if (reason.mayStartNext)
            plaIndex(currentIndex + 1);
    }

    private void broadcast() {
        if (onStateChange != null)
            onStateChange.accept(snapshot());
    }

    public PlayerStateDTO snapshot() {
        List<PlayerStateDTO.TrackDTO> trackDTOs = queue.stream().map(this::toDto).toList();
        AudioTrack current = player.getPlayingTrack();
        return new PlayerStateDTO(
                trackDTOs,
                currentIndex,
                current != null,
                current != null ? current.getPosition() : 0,
                player.isPaused());
    }

    private PlayerStateDTO.TrackDTO toDto(AudioTrack track) {
        AudioTrackInfo info = track.getInfo();
        return new PlayerStateDTO.TrackDTO(track.getIdentifier(), info.title, info.author, info.length, info.uri, null);
    }
}
