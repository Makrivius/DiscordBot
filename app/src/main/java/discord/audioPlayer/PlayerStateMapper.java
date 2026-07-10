package discord.audioPlayer;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord.util.ThumbnailUtil;

public class PlayerStateMapper {
    public static PlayerStateDTO toDto(TrackScheduler scheduler, String channelName) {
        var trackDtos = scheduler.getQueue().stream().map(PlayerStateMapper::toTrackDto).toList();
        AudioTrack current = scheduler.getCurrentTrack();
        return new PlayerStateDTO(
                channelName,
                current != null ? current.getPosition() : 0,
                scheduler.isPaused(),
                scheduler.isShuffle(),
                scheduler.getRepeatMode(),
                trackDtos,
                scheduler.getCurrentIndex());
    }

    private static PlayerStateDTO.TrackDTO toTrackDto(AudioTrack track) {
        var info = track.getInfo();
        return new PlayerStateDTO.TrackDTO(track.getIdentifier(), info.title, info.author, info.length,
                ThumbnailUtil.getThumbnails(track.getIdentifier()));
    }
}
