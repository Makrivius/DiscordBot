package discord.audioPlayer.interfaces;

import discord.audioPlayer.PlayerStateDTO;
import discord.util.ThumbnailUtil;

public interface TrackInterface {

    String getIdentifier();

    String getQueueId();

    String getTitle();

    String getAuthor();

    long getDurationMs();

    default PlayerStateDTO.TrackDTO toTrackDto() {
        return new PlayerStateDTO.TrackDTO(this.getIdentifier(), this.getQueueId(), this.getTitle(), this.getAuthor(),
                this.getDurationMs(), ThumbnailUtil.getThumbnails(this.getIdentifier()));
    }
}
