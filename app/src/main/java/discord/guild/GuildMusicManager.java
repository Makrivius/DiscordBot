package discord.guild;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord.audioPlayer.AudioPlayerManagerHolder;
import discord.audioPlayer.AudioPlayerSendHandler;
import discord.audioPlayer.PlayerStateDTO;
import discord.audioPlayer.PlayerStateMapper;
import discord.audioPlayer.TrackLoader;
import discord.audioPlayer.TrackScheduler;
import net.dv8tion.jda.api.managers.AudioManager;

public class GuildMusicManager {
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final TrackLoader loader;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    private final List<Consumer<PlayerStateDTO>> listeners = new CopyOnWriteArrayList<>();

    private String channelName;

    public GuildMusicManager(AudioPlayerManagerHolder managerHolder) {
        playerManager = managerHolder.get();

        player = playerManager.createPlayer();
        scheduler = new TrackScheduler(player, managerHolder);
        loader = new TrackLoader(playerManager, scheduler);
        sendHandler = new AudioPlayerSendHandler(player);

        player.addListener(scheduler);
        scheduler.SetBroadcastHook(() -> notifyListeners(snapshot()));
    }

    public void connect(AudioManager guildAudioManager, String channelName) {
        this.channelName = channelName;
        guildAudioManager.setSendingHandler(sendHandler);
    }

    public void enqueueById(String trackId, Consumer<AudioTrack> onSuccess, Consumer<String> onFail) {
        loader.enqueueById(trackId, onSuccess, onFail);
    }

    public void playNowById(String trackId, Consumer<AudioTrack> onSuccess, Consumer<String> onFail) {
        loader.playNowById(trackId, onSuccess, onFail);
    }

    public void loadAndQueue(String query, boolean shuffle, Consumer<AudioTrack> onSuccess, Consumer<String> onFail) {
        loader.loadAndQueue(query, shuffle, onSuccess, onFail);
    }

    public void search(String query, Consumer<List<AudioTrack>> onResults, Consumer<String> onFail) {
        loader.search(query, onResults, onFail);
    }

    public boolean isQueueEmpty() {
        return scheduler.isEmpty();
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

    public void clearQueue() {
        scheduler.clearExceptCurrent();
    }

    public void remove(String queueId) {
        scheduler.remove(queueId);
    }

    public void jumpTo(int index) {
        scheduler.jumpTo(index);
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
