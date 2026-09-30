package discord.guild;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import discord.audioPlayer.ManagerHolder;
import discord.audioPlayer.TrackLoader;
import discord.audioPlayer.TrackScheduler;

public class SessionRegistry {
    private final ManagerHolder managerHolder;
    private final ScheduledExecutorService retryExecutor;
    private final Map<Long, TrackScheduler> schedulers = new ConcurrentHashMap<>();
    private final Map<Long, TrackLoader> loaders = new ConcurrentHashMap<>();

    public SessionRegistry(ManagerHolder managerHolder, ScheduledExecutorService retryExecutor) {
        this.managerHolder = managerHolder;
        this.retryExecutor = retryExecutor;
    }

    public TrackScheduler getScheduler(long guildId) {
        return schedulers.computeIfAbsent(guildId,
                id -> new TrackScheduler(id, managerHolder.getActiveAudioPlayer(), retryExecutor));
    }

    public TrackLoader getLoader(long guildId) {
        return loaders.computeIfAbsent(guildId, id -> {
            return new TrackLoader(getScheduler(id), managerHolder.getActiveAudioPlayer(), retryExecutor);
        });
    }
}