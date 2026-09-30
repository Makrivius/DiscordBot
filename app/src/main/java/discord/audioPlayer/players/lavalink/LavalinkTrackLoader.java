package discord.audioPlayer.players.lavalink;

import java.util.function.Consumer;

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkLoadResult;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.NoMatches;
import dev.arbjerg.lavalink.client.player.TrackLoaded;
import discord.audioPlayer.interfaces.TrackInterface;

public class LavalinkTrackLoader {
    private final Link link;

    public LavalinkTrackLoader(Link link) {
        this.link = link;
    }

    public void loadById(String trackId, Consumer<TrackInterface> onSuccess, Consumer<String> onFail) {
        link.loadItem(trackId).subscribe(result -> onSingleTrackLoaded(result, trackId, onFail, onSuccess),
                err -> onFail.accept("Load failed: " + err.getMessage()));
    }

    private void onSingleTrackLoaded(LavalinkLoadResult result, String context, Consumer<String> onFail,
            Consumer<TrackInterface> onTrack) {
        switch (result) {
        case TrackLoaded loaded -> onTrack.accept(LavalinkTrack.from(loaded.getTrack()));
        case NoMatches _ -> onFail.accept("Track not found: " + context);
        case LoadFailed failed -> onFail.accept("Load failed due to: " + failed.getException().getMessage());
        default -> onFail.accept("Unexpected result for: " + context);
        }
    }
}
