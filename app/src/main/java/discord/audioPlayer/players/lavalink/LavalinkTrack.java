package discord.audioPlayer.players.lavalink;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import discord.audioPlayer.interfaces.TrackInterface;

public class LavalinkTrack implements TrackInterface {

    private final Track track;
    private final String queueId;

    public LavalinkTrack(Track track) {
        this.track = track;
        queueId = java.util.UUID.randomUUID().toString();
    }

    public static LavalinkTrack from(Track track) {
        if (track == null)
            return null;
        return new LavalinkTrack(track);
    }

    public Track getTrack() {
        return track;
    }

    @Override
    public String getIdentifier() {
        return getTrackInfo().getIdentifier();
    }

    @Override
    public String getQueueId() {
        return queueId;
    }

    @Override
    public String getTitle() {
        return getTrackInfo().getTitle();
    }

    @Override
    public String getAuthor() {
        return getTrackInfo().getAuthor();
    }

    @Override
    public long getDurationMs() {
        return getTrackInfo().getLength();
    }

    public TrackInfo getTrackInfo() {
        return track.getInfo();
    }
}
