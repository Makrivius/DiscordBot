package discord.audioPlayer;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import discord.util.ThumbnailUtil;

public class PlayerStateMapper {
    public static PlayerStateDTO toDto(TrackScheduler scheduler, String channelName) {
        var trackDtos = scheduler.getQueue().stream().map(PlayerStateMapper::toTrackDto).toList();
        Track current = scheduler.getCurrentTrack();
        return new PlayerStateDTO(
                "state",
                channelName,
                current != null ? scheduler.getCurrentPosition() : 0L,
                scheduler.isPaused(),
                scheduler.isShuffle(),
                scheduler.getRepeatMode(),
                trackDtos,
                scheduler.getCurrentIndex());
    }

    private static PlayerStateDTO.TrackDTO toTrackDto(Track track) {
        TrackInfo info = track.getInfo();
        java.util.Map<?, ?> userData = track.getUserData(java.util.Map.class);
        String queueId = String.valueOf(userData.get("queueId"));
        return new PlayerStateDTO.TrackDTO(info.getIdentifier(), queueId, info.getTitle(), info.getAuthor(),
                info.getLength(),
                ThumbnailUtil.getThumbnails(info.getIdentifier()));
    }
}
