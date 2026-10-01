package discord.audioPlayer.players.lavalink;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.LinkState;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.player.LavalinkLoadResult;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.NoMatches;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.SearchResult;
import dev.arbjerg.lavalink.client.player.TrackLoaded;
import discord.audioPlayer.interfaces.PlayerInterface;
import discord.audioPlayer.interfaces.TrackInterface;

public class LavalinkPlayerWrapper implements PlayerInterface {

    Logger log = LoggerFactory.getLogger(LavalinkPlayerWrapper.class);

    private final LavalinkClient lavalinkClient;
    private final Link link;

    private Runnable broadcastFunction;
    private Runnable onTrackEndReady = () -> {
    };

    public LavalinkPlayerWrapper(LavalinkClient lavalinkManager, Link link) {
        this.lavalinkClient = lavalinkManager;
        this.link = link;
        setupEvents();
    }

    private void setupEvents() {
        lavalinkClient.on(TrackEndEvent.class).filter(e -> e.getGuildId() == link.getGuildId()).subscribe(e -> {
            if (e.getEndReason().getMayStartNext()) {
                onTrackEndReady.run();
            }
        });
    }

    @Override
    public TrackInterface getCurrentTrack() {
        var player = link.getCachedPlayer();
        return player != null ? LavalinkTrack.from(player.getTrack()) : null;
    }

    @Override
    public long getCurrentPosition() {
        var player = link.getCachedPlayer();
        return player != null ? player.getPosition() : 0L;
    }

    @Override
    public void setPosition(long position) {
        var player = link.getCachedPlayer();
        player.setPosition(position).subscribe(p -> broadcastFunction.run(),
                err -> log.error("Failed to set position/seek", err));
    }

    @Override
    public boolean isPaused() {
        var player = link.getCachedPlayer();
        return player != null && Boolean.TRUE.equals(player.getPaused());
    }

    @Override
    public CompletableFuture<PlayResult> requestPlay(TrackInterface track) {
        if (!(track instanceof LavalinkTrack lt)) {
            return CompletableFuture.completedFuture(PlayResult.FAILED);
        }
        if (link == null || link.getState() == LinkState.CONNECTING) {
            return CompletableFuture.completedFuture(PlayResult.RETRY);
        }
        var future = new CompletableFuture<PlayResult>();
        link.createOrUpdatePlayer().setTrack(lt.getTrack()).subscribe(p -> future.complete(PlayResult.STARTED), err -> {
            log.error("Failed to play track", err);
            future.complete(PlayResult.FAILED);
        });
        return future;
    }

    @Override
    public void resume() {
        link.createOrUpdatePlayer().setPaused(false).subscribe(p -> broadcastFunction.run(),
                err -> log.error("Failed to resume", err));
    }

    @Override
    public void pause() {
        link.createOrUpdatePlayer().setPaused(true).subscribe(p -> broadcastFunction.run(),
                err -> log.error("Failed to pause", err));
    }

    @Override
    public void setBroadcastFunction(Runnable broadcastFunction) {
        this.broadcastFunction = broadcastFunction;
    }

    @Override
    public void setOnTrackEnd(Runnable onTrackEndReady) {
        this.onTrackEndReady = onTrackEndReady;
    }

    @Override
    public CompletableFuture<LoadResult> load(String query) {
        String lookup = toIdentifier(query);
        var future = new CompletableFuture<LoadResult>();
        link.loadItem(lookup).subscribe(result -> future.complete(toLoadResult(result)),
                err -> future.complete(new LoadResult(LoadStatus.FAILED, List.of(), err.getMessage())));
        return future;
    }

    private LoadResult toLoadResult(LavalinkLoadResult result) {
        return switch (result) {
        case TrackLoaded t -> new LoadResult(LoadStatus.OK, List.of(LavalinkTrack.from(t.getTrack())), null);
        case SearchResult s -> new LoadResult(LoadStatus.OK,
                s.getTracks().stream().<TrackInterface>map(LavalinkTrack::from).toList(), null);
        case PlaylistLoaded p -> new LoadResult(LoadStatus.PLAYLIST,
                p.getTracks().stream().<TrackInterface>map(LavalinkTrack::from).toList(), null);
        case NoMatches _ -> new LoadResult(LoadStatus.NO_MATCHES, List.of(), null);
        case LoadFailed f -> new LoadResult(LoadStatus.FAILED, List.of(), f.getException().getMessage());
        default -> new LoadResult(LoadStatus.FAILED, List.of(), "Unexpected result");
        };
    }

    private static String toIdentifier(String q) {
        if (q.startsWith("http") || q.contains(":") || q.startsWith("ytsearch"))
            return q;
        return "https://www.youtube.com/watch?v=" + q;
    }

}