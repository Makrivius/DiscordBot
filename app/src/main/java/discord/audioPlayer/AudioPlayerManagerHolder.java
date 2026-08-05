package discord.audioPlayer;

import java.lang.reflect.Field;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.lavalink.youtube.YoutubeAudioSourceManager;

public class AudioPlayerManagerHolder {
    private final Logger log = LoggerFactory.getLogger(AudioPlayerManagerHolder.class);

    private final AudioPlayerManager searchPlayerManager;
    private final LavalinkClient lavalinkClient;

    public AudioPlayerManagerHolder(String botToken) {
        searchPlayerManager = createPlayerManager();
        lavalinkClient = createLavalinkClient(botToken);
    }

    private AudioPlayerManager createPlayerManager() {
        DefaultAudioPlayerManager searchPlayerManager = new DefaultAudioPlayerManager();
        YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager();
        searchPlayerManager.registerSourceManager(yt);
        return searchPlayerManager;
    }

    private LavalinkClient createLavalinkClient(String botToken) {
        LavalinkClient lavalinkClient = new LavalinkClient(Helpers.getUserIdFromToken(botToken));
        return lavalinkClient;
    }

    public LavalinkClient getAudioPlayerManager() {
        return lavalinkClient;
    }

    public AudioPlayerManager getSearchPlayerManager() {
        return searchPlayerManager;
    }

    public void debugPrintSources() {
        try {
            Field fields = DefaultAudioPlayerManager.class.getDeclaredField("sourceManagers");
            fields.setAccessible(true);

            @SuppressWarnings("unchecked")
            List<LavalinkClient> sources = (List<LavalinkClient>) fields.get(this.lavalinkClient);

            log.info("Current registered sources count: {}", sources.size());
            LavalinkClient src = sources.getFirst();
            log.info("[{}] {} (Instance ID: {})",
                    0, src.getClass().getName(), Integer.toHexString(System.identityHashCode(src)));
        } catch (Exception e) {
            log.error("Failed to print debug sources", e);
        }
    }
}
