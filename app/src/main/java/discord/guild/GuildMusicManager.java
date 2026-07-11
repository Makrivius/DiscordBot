package discord.guild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord.audioPlayer.AudioPlayerManagerHolder;
import discord.audioPlayer.AudioPlayerSendHandler;
import discord.audioPlayer.PlayerStateDTO;
import discord.audioPlayer.PlayerStateMapper;
import discord.audioPlayer.TrackScheduler;
import net.dv8tion.jda.api.managers.AudioManager;

public class GuildMusicManager {
    private static final Logger log = LoggerFactory.getLogger(GuildMusicManager.class);

    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    private final List<Consumer<PlayerStateDTO>> listeners = new CopyOnWriteArrayList<>();

    private String channelName;

    public GuildMusicManager(AudioPlayerManagerHolder managerHolder) {
        this.playerManager = managerHolder.get();
        player = playerManager.createPlayer();
        scheduler = new TrackScheduler(player, managerHolder);
        sendHandler = new AudioPlayerSendHandler(player);

        player.addListener(scheduler);
        scheduler.setOnStateChanged(() -> notifyListeners(snapshot()));
    }

    public void connect(AudioManager guildAudioManager, String channelName) {
        this.channelName = channelName;
        guildAudioManager.setSendingHandler(sendHandler);
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
    }

    public void pause() {
        scheduler.pause();
    }

    public void resume() {
        scheduler.resume();
    }

    public void previous() {
        scheduler.previous();
    }

    public void next() {
        scheduler.next();
    }

    public void toggleShuffle() {
        scheduler.toggleShuffle();
    }

    public void cycleRepeat() {
        scheduler.cycleRepeat();
    }

    public void reorder(List<String> newQueueIds) {
        scheduler.reorder(newQueueIds);
    }

    public void seek(long ms) {
        scheduler.seek(ms);
    }

    public PlayerStateDTO snapshot() {
        return PlayerStateMapper.toDto(scheduler, channelName);
    }

    private String normalizeQuery(String query) {
        if (query.startsWith("http") && query.contains("list=") && !query.contains("playlist?list=")) {
            return query.replaceAll("[&?]list=[^&]+", "");
        }
        return query.startsWith("http") ? query : "ytsearch:" + query;
    }

    public void onStateChange(Consumer<PlayerStateDTO> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<PlayerStateDTO> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(PlayerStateDTO state) {
        listeners.forEach(l -> l.accept(state));
    }

}
