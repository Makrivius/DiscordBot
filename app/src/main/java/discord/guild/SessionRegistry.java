package discord.guild;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.stereotype.Component;

import discord.audioPlayer.PlayerFactory;
import discord.audioPlayer.TrackLoader;
import discord.audioPlayer.TrackScheduler;
import discord.audioPlayer.interfaces.PlayerInterface;

@Component
public class SessionRegistry {
    private record Session(TrackScheduler scheduler, TrackLoader loader) {
    }

    private final PlayerFactory playerFactory;
    private final ScheduledExecutorService retryExecutor;
    private final Map<Long, Session> sessions = new ConcurrentHashMap<>();

    public SessionRegistry(PlayerFactory playerFactory, ScheduledExecutorService retryExecutor) {
        this.playerFactory = playerFactory;
        this.retryExecutor = retryExecutor;
    }

    private Session session(long guildId) {
        return sessions.computeIfAbsent(guildId, id -> {
            PlayerInterface player = playerFactory.create(id);
            var scheduler = new TrackScheduler(id, player, retryExecutor);
            return new Session(scheduler, new TrackLoader(scheduler, player, retryExecutor));
        });
    }

    public TrackScheduler getScheduler(long id) {
        return session(id).scheduler();
    }

    public TrackLoader getLoader(long id) {
        return session(id).loader();
    }
}