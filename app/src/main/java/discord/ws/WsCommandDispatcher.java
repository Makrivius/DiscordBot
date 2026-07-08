package discord.ws;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord.guild.GuildMusicManager;

public class WsCommandDispatcher {
    private final Logger log = LoggerFactory.getLogger(WsCommandDispatcher.class);
    private final Map<String, BiConsumer<GuildMusicManager, String>> handlers = new HashMap<>();

    public WsCommandDispatcher() {
        handlers.put("pause", (mgr, arg) -> mgr.pause());
        handlers.put("resume", (mgr, arg) -> mgr.resume());
        handlers.put("skip", (mgr, arg) -> mgr.skip());
    }

    public void dispatch(GuildMusicManager manager, String rawCommand) {
        String[] parts = rawCommand.split(":", 2);
        String action = parts[0];
        String arg = parts.length > 1 ? parts[1] : "";

        var handler = handlers.get(action);
        if (handler == null) {
            log.warn("Unknown WS command: {}", action);
            return;
        }
        handler.accept(manager, arg);
    }
}
