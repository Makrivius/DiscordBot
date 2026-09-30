package discord.audioPlayer;

import discord.audioPlayer.interfaces.TrackInterface;

public class PlayerStateMapper {
    public static PlayerStateDTO toDto(TrackScheduler scheduler, String channelName) {
        var trackDtos = scheduler.getQueue().stream().map(t -> t.toTrackDto()).toList();
        TrackInterface current = scheduler.getCurrentTrack();
        return new PlayerStateDTO("state", channelName, current != null ? scheduler.getCurrentPosition() : 0L,
                scheduler.isPaused(), scheduler.isShuffle(), scheduler.getRepeatMode(), trackDtos,
                scheduler.getCurrentIndex());
    }

}
