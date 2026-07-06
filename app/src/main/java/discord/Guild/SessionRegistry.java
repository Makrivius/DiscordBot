package discord.Guild;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class SessionRegistry {
    private final AudioPlayerManager audioPlayerManager;
    private final Map<Long, GuildMusicManager> sessions = new ConcurrentHashMap<>();

    public SessionRegistry(AudioPlayerManager audioPlayerManager) {
        this.audioPlayerManager = audioPlayerManager;
    }

    public GuildMusicManager get(long guildId) {
        return sessions.computeIfAbsent(guildId, id -> new GuildMusicManager(audioPlayerManager));
    }
}
