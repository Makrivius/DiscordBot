package discord.audioPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord.audioPlayer.interfaces.PlayerInterface;
import discord.audioPlayer.interfaces.PlayerInterface.PlayResult;
import discord.audioPlayer.interfaces.TrackInterface;

public class TrackScheduler {
    private static Logger log = LoggerFactory.getLogger(TrackScheduler.class);
    private static final int MAX_CONNECTING_RETRIES = 5;

    private final List<Consumer<PlayerStateDTO>> listeners = new CopyOnWriteArrayList<>();

    private final PlayerInterface player;
    private final ScheduledExecutorService retryExecutor;
    private final List<TrackInterface> queue = new ArrayList<>();

    private String repeatMode = "off";
    private int currentIndex = -1;

    private String channelName;

    public TrackScheduler(long guildId, PlayerInterface player, ScheduledExecutorService retryExecutor) {
        this.player = player;
        this.retryExecutor = retryExecutor;
        player.setOnTrackEnd(this::onTrackEnd);
        player.setBroadcastFunction(this::broadcast);
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }

    public synchronized List<TrackInterface> getQueue() {
        return List.copyOf(queue);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized TrackInterface getCurrentTrack() {
        return currentIndex >= 0 && currentIndex < queue.size() ? queue.get(currentIndex) : null;
    }

    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public boolean isPaused() {
        return player.isPaused();
    }

    public boolean isShuffle() {
        return snapshot().shuffle();
    }

    public String getRepeatMode() {
        return repeatMode;
    }

    public synchronized int getCurrentIndex() {
        return currentIndex;
    }

    public void onStateChange(Consumer<PlayerStateDTO> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<PlayerStateDTO> listener) {
        listeners.remove(listener);
    }

    public synchronized PlayerStateDTO snapshot() {
        return toDto();
    }

    public synchronized void enqueue(TrackInterface track) {
        queue.add(track);
        if (player.getCurrentTrack() == null)
            playIndex(queue.size() - 1);
        else
            broadcast();
    }

    public void pause() {
        player.pause();
    }

    public void resume() {
        player.resume();
    }

    public synchronized void previous() {
        Long position = player != null ? player.getCurrentPosition() : null;
        if (position != null && position >= 3000) {
            player.setPosition(0L);
            return;
        }

        if (queue.isEmpty())
            return;

        int prevIndex = getCurrentTrack() == null ? currentIndex : currentIndex - 1;
        if (prevIndex < 0)
            prevIndex = 0;

        playIndex(prevIndex);
    }

    public synchronized void next() {
        advance();
    }

    public synchronized void toggleShuffle() {
        broadcast();
    }

    public synchronized void cycleRepeat() {
        repeatMode = switch (repeatMode) {
        case "off" -> "track";
        case "track" -> "queue";
        default -> "off";
        };
        broadcast();
    }

    public synchronized void clearExceptCurrent() {
        if (queue.isEmpty()) {
            broadcast();
            return;
        }

        TrackInterface current = currentIndex >= 0 && currentIndex < queue.size() ? queue.get(currentIndex) : null;

        queue.clear();
        if (current != null) {
            queue.add(current);
            currentIndex = 0;
        } else {
            currentIndex = -1;
        }
        broadcast();
    }

    public synchronized void remove(String queueId) {
        int removeIndex = indexOfQueueId(queueId);
        if (removeIndex < 0)
            return;

        boolean removeCurrent = removeIndex == currentIndex;

        queue.remove(removeIndex);

        if (removeCurrent) {
            currentIndex = removeIndex - 1;
            advance();
            return;
        }

        if (removeIndex < currentIndex) {
            currentIndex--;
        }
        broadcast();
    }

    public synchronized void jumpTo(int index) {
        if (queue.size() <= index || index < 0) {
            log.error("Invalid position: {} for queue, queue length is: {}", index, queue.size());
            return;
        }

        currentIndex = index;
        playIndex(currentIndex);
    }

    public synchronized void reorder(List<String> newQueueIds) {
        Map<String, TrackInterface> byQueueId = queue.stream().collect(Collectors.toMap(t -> t.getQueueId(), t -> t));

        String currentQueueId = currentIndex >= 0 ? queue.get(currentIndex).getQueueId() : null;

        List<TrackInterface> reordered = newQueueIds.stream().map(byQueueId::get).filter(Objects::nonNull).toList();

        queue.clear();
        queue.addAll(reordered);

        if (currentQueueId != null) {
            currentIndex = queue.stream().map(t -> t.getQueueId()).toList().indexOf(currentQueueId);
        }
        broadcast();
    }

    public void seek(long positionMs) {
        TrackInterface current = getCurrentTrack();
        if (current != null) {
            player.setPosition(positionMs);
        }
    }

    public PlayerStateDTO toDto() {
        var trackDtos = getQueue().stream().map(t -> t.toTrackDto()).toList();
        TrackInterface current = getCurrentTrack();
        return new PlayerStateDTO("state", channelName, current != null ? getCurrentPosition() : 0L, isPaused(),
                isShuffle(), getRepeatMode(), trackDtos, getCurrentIndex());
    }

    private int indexOfQueueId(String queueId) {
        for (int i = 0; i < queue.size(); i++) {
            var data = queue.get(i).getQueueId();
            if (data != null) {
                if (queueId.equals(data)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void playIndex(int i) {
        playIndexWithRetry(i, 0);
    }

    private void playIndexWithRetry(int i, int attempt) {
        if (i < 0 || i >= queue.size())
            return;
        currentIndex = i;

        player.requestPlay(queue.get(i)).whenComplete((result, err) -> {
            if (err != null)
                result = PlayResult.FAILED;
            switch (result) {
            case STARTED -> broadcast();
            case RETRY -> {
                if (attempt >= MAX_CONNECTING_RETRIES) {
                    log.error("Giving up on index {} after {} attempts", i, attempt);
                    return;
                }
                retryExecutor.schedule(() -> playIndexWithRetry(i, attempt + 1), 1500, TimeUnit.MILLISECONDS);
            }
            case FAILED -> log.error("Play failed for index {}", i);
            }
        });
    }

    private void advance() {
        int nextIndex = currentIndex + 1;
        if (nextIndex >= queue.size()) {
            if (repeatMode.equals("queue")) {
                playIndex(0);
            } else {
                currentIndex = -1;
                player.pause();
            }
            return;
        }
        playIndex(nextIndex);
    }

    private void notifyListeners(PlayerStateDTO state) {
        listeners.forEach(l -> l.accept(state));
    }

    private void broadcast() {
        notifyListeners(snapshot());
    }

    private void onTrackEnd() {
        if (repeatMode.equals("track")) {
            playIndex(currentIndex);
            return;
        }
        advance();
    }
}
