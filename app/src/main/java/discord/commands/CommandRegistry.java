package discord.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CommandRegistry {
    private static final Logger log = LoggerFactory.getLogger(CommandRegistry.class);
    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry(List<Command> discovered) {
        for (Command c : discovered) {
            try {
                commands.put(c.name().toLowerCase(), c);
                log.info("Registered command: {}", c.name());
            } catch (Exception e) {
                log.error("Failed to load command {}", e);
            }
        }
        log.info("Loaded {} commands total", commands.size());
    }

    public Command get(String name) {
        return commands.get(name.toLowerCase());
    }

    public Map<String, Command> all() {
        return Map.copyOf(commands);
    }

}
