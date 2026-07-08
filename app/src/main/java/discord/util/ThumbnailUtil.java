package discord.util;

import discord.audioPlayer.PlayerStateDTO;

public class ThumbnailUtil {
    public static PlayerStateDTO.Artwork getThumbnails(String videoId) {
        return new PlayerStateDTO.Artwork("https://img.youtube.com/vi/" + videoId + "/mqdefault.jpg",
                "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg",
                "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg");
    }

}
