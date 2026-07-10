package discord.guild;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import discord.audioPlayer.AudioPlayerManagerHolder;

public class SessionRegistry {
    private final AudioPlayerManagerHolder managerHolder;
    private final Map<Long, GuildMusicManager> sessions = new ConcurrentHashMap<>();

    public SessionRegistry(AudioPlayerManagerHolder managerHolder) {
        this.managerHolder = managerHolder;
    }

    public GuildMusicManager get(long guildId) {
        return sessions.computeIfAbsent(guildId, id -> new GuildMusicManager(managerHolder));
    }
}
