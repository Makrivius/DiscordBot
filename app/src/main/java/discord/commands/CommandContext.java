package discord.commands;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import discord.audioPlayer.TrackLoader;
import discord.audioPlayer.TrackScheduler;

public class CommandContext {
    private final long guildId;
    private final TrackScheduler trackScheduler;
    private final TrackLoader trackLoader;
    private final List<String> positionalArgs;
    private final Map<String, String> namedArgs;
    private final Consumer<String> reply;
    private final Consumer<String> onError;
    private final Consumer<Object> replyData;

    public CommandContext(long guildId, TrackScheduler trackScheduler, TrackLoader trackLoader,
            List<String> positionalArgs, Map<String, String> namedArgs, Consumer<String> reply,
            Consumer<String> onError, Consumer<Object> replyData) {
        this.guildId = guildId;
        this.trackScheduler = trackScheduler;
        this.trackLoader = trackLoader;
        this.positionalArgs = positionalArgs;
        this.namedArgs = namedArgs;
        this.reply = reply;
        this.onError = onError;
        this.replyData = replyData;
    }

    public long guildId() {
        return guildId;
    }

    public TrackScheduler trackScheduler() {
        return trackScheduler;
    }

    public TrackLoader trackLoader() {
        return trackLoader;
    }

    public List<String> positionArgs() {
        return positionalArgs;
    }

    public String named(String key) {
        return namedArgs.get(key);
    }

    public boolean has(String flag) {
        return namedArgs.containsKey(flag);
    }

    public void reply(String msg) {
        reply.accept(msg);
    }

    public void error(String msg) {
        onError.accept(msg);
    }

    public void replyData(Object payload) {
        replyData.accept(payload);
    }
}
