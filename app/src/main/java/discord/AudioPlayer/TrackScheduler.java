package discord.audioPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import dev.lavalink.youtube.AllClientsFailedException;
import discord.util.ThumbnailUtil;

public class TrackScheduler extends AudioEventAdapter {
    private static Logger log = LoggerFactory.getLogger(TrackScheduler.class);
    private final AudioPlayer player;
    private final AudioPlayerManagerHolder managerHolder;
    private final List<AudioTrack> queue = new ArrayList<>();
    private int currentIndex = -1;
    private Consumer<PlayerStateDTO> onStateChange;

    public TrackScheduler(AudioPlayer player, AudioPlayerManagerHolder managerHolder) {
        this.player = player;
        this.managerHolder = managerHolder;
    }

    public void setOnStateChanged(Consumer<PlayerStateDTO> callback) {
        this.onStateChange = callback;
    }

    public void enqueue(AudioTrack track) {
        queue.add(track);
        if (player.getPlayingTrack() == null)
            plaIndex(queue.size() - 1);
        broadcast();
    }

    private void plaIndex(int i) {
        if (i < 0 || i >= queue.size())
            return;
        currentIndex = i;
        player.playTrack(queue.get(i));
        broadcast();
    }

    private void broadcast() {
        if (onStateChange != null)
            onStateChange.accept(snapshot());
    }

    public void pause() {
        player.setPaused(true);
        broadcast();
    }

    public void resume() {
        player.setPaused(false);
        broadcast();
    }

    public void skip() {
        player.stopTrack();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason reason) {
        log.info("Track ended: {} | reason: {} | mayStartNext: {} | currentIndex: {}",
                track.getInfo().title, reason, reason.mayStartNext, currentIndex);
        if (reason.mayStartNext)
            plaIndex(currentIndex + 1);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof AllClientsFailedException) {
            log.error("All YT clients are failed to load, hard refresh");

            managerHolder.refreshYoutubeSource();

            AudioTrack retriedTrack = track.makeClone();
            queue.set(currentIndex, retriedTrack);
            player.playTrack(retriedTrack);
            broadcast();
        }
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
        return new PlayerStateDTO.TrackDTO(track.getIdentifier(), info.title, info.author, info.length, info.uri,
                ThumbnailUtil.getThumbnails(track.getIdentifier()));
    }
}
