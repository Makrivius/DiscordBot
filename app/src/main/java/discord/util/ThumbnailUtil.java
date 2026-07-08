package discord.util;

import java.util.Map;

public class ThumbnailUtil {
    public static Map<String, String> getThumbnails(String videoId) {
        return Map.of("low", "https://img.youtube.com/vi/" + videoId + "/mqdefault.jpg",
                "high", "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg",
                "highdef", "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg");
    }

}
