package discord.audioPlayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

public class AudioPlayerManagerHolder {
    private final AudioPlayerManager playerManager;

    public AudioPlayerManagerHolder() {
        playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public AudioPlayerManager get() {
        return playerManager;
    }
}
