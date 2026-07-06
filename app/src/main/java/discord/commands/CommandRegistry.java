package discord.commands;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandRegistry {
    private static final Logger log = LoggerFactory.getLogger(CommandRegistry.class);
    private final Map<String, Command> commands = new HashMap<>();
    private final String prefix;

    public CommandRegistry(String prefix) {
        this.prefix = prefix;
    }

    public void register(Command command) {
        commands.put(command.name().toLowerCase(), command);
        log.info("Registered command: {}{}", prefix, command.name());
    }

    public void handle(MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();
        if (!content.startsWith(prefix))
            return;

        String[] parts = content.substring(prefix.length()).trim().split("\\+s");
        if (parts.length == 0 || parts[0].isEmpty())
            return;

        String cmdName = parts[0].toLowerCase();
        String[] args = java.util.Arrays.copyOfRange(parts, 1, parts.length);

        Command command = commands.get(cmdName);
        if (command == null)
            return;

        try {
            command.execute(event, args);
        } catch (Exception e) {
            log.error("Command '{}' threw an exception", cmdName, e);
            event.getChannel().sendMessage("Something went wrong with a command").queue();
        }
    }
}
