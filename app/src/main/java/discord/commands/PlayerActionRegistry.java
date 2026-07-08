package discord.commands;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord.guild.GuildMusicManager;

public class PlayerActionRegistry {
    private static final Logger log = LoggerFactory.getLogger(PlayerActionRegistry.class);
    private final Map<String, PlayerAction> actions = new HashMap<>();

    public PlayerActionRegistry() {
        register("pause", (mgr, args) -> mgr.pause());
        register("resume", (mgr, args) -> mgr.resume());
        register("prev", (mgr, args) -> mgr.previous());
        register("next", (mgr, args) -> mgr.next());
        register("shuffle", (mgr, args) -> mgr.toggleShuffle());
        register("repeat", (mgr, args) -> mgr.cycleRepeat());
        register("seek", (mgr, args) -> mgr.seek(Long.parseLong(args.get("positionMs").toString())));
    }

    private void register(String name, PlayerAction action) {
        actions.put(name, action);
    }

    public void dispatch(GuildMusicManager manager, String actionName, Map<String, Object> args) {
        PlayerAction action = actions.get(actionName);
        if (action == null) {
            log.warn("Unknown player action: {}", actionName);
            return;
        }
        try {
            action.execute(manager, args);
        } catch (Exception e) {
            log.error("Action '{}' failed", actionName, e);
        }
    }
}
