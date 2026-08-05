package discord.audioPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.player.Track;

public class TrackScheduler {
    private static Logger log = LoggerFactory.getLogger(TrackScheduler.class);

    private final Link link;
    private final List<Track> queue = new ArrayList<>();
    private boolean shuffle = false;
    private String repeatMode = "off";
    private int currentIndex = -1;
    private Runnable onStateChange;

    public List<Track> getQueue() {
        return List.copyOf(queue);
    }

    public Track getCurrentTrack() {
        var player = link.getCachedPlayer();
        return player != null ? player.getTrack() : null;
    }

    public long getCurrentPosition() {
        var player = link.getCachedPlayer();
        return player != null ? player.getPosition() : 0L;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean isPaused() {
        var player = link.getCachedPlayer();
        return player != null && Boolean.TRUE.equals(player.getPaused());
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

    public TrackScheduler(Link link, LavalinkClient lavalinkClient) {
        this.link = link;
        lavalinkClient.on(TrackEndEvent.class).filter(event -> event.getGuildId() == link.getGuildId())
                .subscribe(this::onTrackEnd);
    }

    public void SetBroadcastHook(Runnable callback) {
        this.onStateChange = callback;
    }

    public void enqueue(Track track) {
        String queueId = java.util.UUID.randomUUID().toString();
        track.setUserData(java.util.Map.of("queueId", queueId));
        queue.add(track);
        if (getCurrentTrack() == null)
            playIndex(queue.size() - 1);
        else
            broadcast();
    }

    public void pause() {
        link.createOrUpdatePlayer().setPaused(true)
                .subscribe(p -> broadcast(), err -> log.error("Failed to pause", err));
    }

    public void resume() {
        link.createOrUpdatePlayer().setPaused(false)
                .subscribe(p -> broadcast(), err -> log.error("Failed to resume", err));
    }

    public void previous() {
        var player = link.getCachedPlayer();
        Long position = player != null ? player.getPosition() : null;
        if (position != null && position >= 3000) {
            link.createOrUpdatePlayer().setPosition(0L)
                    .subscribe(p -> broadcast(), err -> log.error("Failed to seek", err));
            return;
        }

        if (queue.isEmpty())
            return;

        int prevIndex = getCurrentTrack() == null ? currentIndex : currentIndex - 1;
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

    public void clearExceptCurrent() {
        if (queue.isEmpty()) {
            broadcast();
            return;
        }

        Track current = currentIndex >= 0 && currentIndex < queue.size() ? queue.get(currentIndex) : null;

        queue.clear();
        if (current != null) {
            queue.add(current);
            currentIndex = 0;
        } else {
            currentIndex = -1;
        }
        broadcast();
    }

    public void remove(String queueId) {
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

    public void jumpTo(int index) {
        if (queue.size() < index || index < 0) {
            log.error("Invalid position: {} for queue, queue length is: {}", index, queue.size());
        }

        currentIndex = index;
        playIndex(currentIndex);
    }

    public void reorder(List<String> newQueueIds) {
        Map<String, Track> byQueueId = queue.stream()
                .collect(Collectors.toMap(t -> t.getUserData().toString(), t -> t));

        String currentQueueId = currentIndex >= 0 ? queue.get(currentIndex).getUserData().toString() : null;

        List<Track> reordered = newQueueIds.stream().map(byQueueId::get).filter(Objects::nonNull).toList();

        queue.clear();
        queue.addAll(reordered);

        if (currentQueueId != null) {
            currentIndex = queue.stream().map(t -> t.getUserData().toString()).toList().indexOf(currentQueueId);
        }
        broadcast();
    }

    public void seek(long positionMs) {
        Track current = link.getCachedPlayer() != null ? link.getCachedPlayer().getTrack() : null;
        if (current != null) {
            link.createOrUpdatePlayer().setPosition(positionMs)
                    .subscribe(p -> broadcast(), err -> log.error("Failed to seek", err));
        }
    }

    private int indexOfQueueId(String queueId) {
        for (int i = 0; i < queue.size(); i++) {
            JsonNode data = queue.get(i).getUserData();
            if (data != null && data.has("queueId")) {
                String storedId = data.get("queueId").asText();
                if (queueId.equals(storedId)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void playIndex(int i) {
        if (i < 0 || i >= queue.size())
            return;
        currentIndex = i;
        Track retriedTrack = queue.get(i).makeClone();
        retriedTrack.setUserData(queue.get(i).getUserData());
        link.createOrUpdatePlayer().setTrack(retriedTrack).subscribe(player -> broadcast(),
                err -> log.error("Failed to play track", err));
    }

    private void advance() {
        int nextIndex = currentIndex + 1;
        if (nextIndex >= queue.size()) {
            if (repeatMode.equals("queue")) {
                playIndex(0);
            } else {
                currentIndex = -1;
                link.createOrUpdatePlayer().setPaused(true).subscribe(player -> broadcast(),
                        err -> log.error("Failed to pause", err));
            }
            return;
        }
        playIndex(nextIndex);
    }

    private void broadcast() {
        if (onStateChange != null)
            onStateChange.run();
    }

    private void onTrackEnd(TrackEndEvent event) {
        if (!event.getEndReason().getMayStartNext()) {
            return;
        }
        if (repeatMode.equals("track")) {
            playIndex(currentIndex);
            return;
        }
        advance();
    }
}
