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

    public AudioPlayerManagerHolder() {
        playerManager = new DefaultAudioPlayerManager();

        this.yt = new YoutubeAudioSourceManager();
        playerManager.registerSourceManager(yt);

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public AudioPlayerManager get() {
        return playerManager;
    }

    public synchronized void refreshYoutubeSource() {
        try {
            Field fields = DefaultAudioPlayerManager.class.getDeclaredField("sourceManagers");
            fields.setAccessible(true);

            @SuppressWarnings("unchecked")
            List<AudioSourceManager> sources = (List<AudioSourceManager>) fields.get(this.playerManager);
            sources.removeIf(source -> source instanceof dev.lavalink.youtube.YoutubeAudioSourceManager);

            this.yt = new dev.lavalink.youtube.YoutubeAudioSourceManager();
            sources.add(0, this.yt);
            log.info("Yt source manager forcefully refreshed");
        } catch (Exception e) {
            log.error(e.toString());
        }
    }
}
