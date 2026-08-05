package discord.guild;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.Track;
import discord.audioPlayer.AudioPlayerManagerHolder;
import discord.audioPlayer.PlayerStateDTO;
import discord.audioPlayer.PlayerStateMapper;
import discord.audioPlayer.TrackLoader;
import discord.audioPlayer.TrackScheduler;

public class GuildMusicManager {
    private final AudioPlayerManager searchManager;
    private final LavalinkClient lavalinkClient;
    private final Link link;
    private final TrackLoader loader;
    private final TrackScheduler scheduler;
    private final List<Consumer<PlayerStateDTO>> listeners = new CopyOnWriteArrayList<>();

    private String channelName;

    public GuildMusicManager(long guildId, AudioPlayerManagerHolder managerHolder) {
        searchManager = managerHolder.getSearchPlayerManager();
        lavalinkClient = managerHolder.getAudioPlayerManager();
        link = lavalinkClient.getOrCreateLink(guildId);

        scheduler = new TrackScheduler(link, lavalinkClient);
        loader = new TrackLoader(searchManager, link, scheduler);

        scheduler.SetBroadcastHook(() -> notifyListeners(snapshot()));
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public void enqueueById(String trackId, Consumer<Track> onSuccess, Consumer<String> onFail) {
        loader.enqueueById(trackId, onSuccess, onFail);
    }

    public void playNowById(String trackId, Consumer<Track> onSuccess, Consumer<String> onFail) {
        loader.playNowById(trackId, onSuccess, onFail);
    }

    public void loadAndQueue(String query, boolean shuffle, Consumer<Track> onSuccess, Consumer<String> onFail) {
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
