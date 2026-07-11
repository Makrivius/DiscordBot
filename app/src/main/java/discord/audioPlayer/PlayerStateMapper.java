package discord.audioPlayer;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

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
        AudioTrackInfo info = track.getInfo();
        String queueId = (String) track.getUserData();
        return new PlayerStateDTO.TrackDTO(track.getIdentifier(), queueId, info.title, info.author, info.length,
                ThumbnailUtil.getThumbnails(track.getIdentifier()));
    }
}
