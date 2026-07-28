package discord.audioPlayer;

import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import dev.lavalink.youtube.YoutubeAudioSourceManager;

public class AudioPlayerManagerHolder {
    private final Logger log = LoggerFactory.getLogger(AudioPlayerManagerHolder.class);

    private final AudioPlayerManager playerManager;
    private YoutubeAudioSourceManager yt;

    private void checkOauth(String refreshToken) {
        if (!refreshToken.isEmpty() && !refreshToken.isBlank()) {
            this.yt.useOauth2(refreshToken, true);
        } else {
            this.yt.useOauth2(refreshToken, false);
        }
    }

    public AudioPlayerManagerHolder(String refreshToken) {
        playerManager = new DefaultAudioPlayerManager();

        this.yt = new YoutubeAudioSourceManager(true,
                new dev.lavalink.youtube.clients.skeleton.Client[] {
                        new dev.lavalink.youtube.clients.Tv(),
                        new dev.lavalink.youtube.clients.Music(),
                        new dev.lavalink.youtube.clients.AndroidMusic(),
                });
        playerManager.registerSourceManager(yt);
        checkOauth(refreshToken);

        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public AudioPlayerManager get() {
        return playerManager;
    }

    public void debugPrintSources() {
        try {
            Field fields = DefaultAudioPlayerManager.class.getDeclaredField("sourceManagers");
            fields.setAccessible(true);

            @SuppressWarnings("unchecked")
            List<AudioSourceManager> sources = (List<AudioSourceManager>) fields.get(this.playerManager);

            log.info("Current registered sources count: {}", sources.size());
            AudioSourceManager src = sources.getFirst();
            log.info("[{}] {} (Instance ID: {})",
                    0, src.getClass().getName(), Integer.toHexString(System.identityHashCode(src)));
        } catch (Exception e) {
            log.error("Failed to print debug sources", e);
        }
    }
}
