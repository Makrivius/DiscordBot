package discord.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord.guild.SessionRegistry;
import discord.guild.VoiceConnector;
import discord.util.ArgParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DiscordCommandDispatcher {
    private static final Logger log = LoggerFactory.getLogger(DiscordCommandDispatcher.class);
    private final CommandRegistry registry;
    private final SessionRegistry sessions;
    private final VoiceConnector voiceConnector;
    private final String prefix;

    public DiscordCommandDispatcher(CommandRegistry registry, SessionRegistry sessions, String prefix) {
        this.registry = registry;
        this.sessions = sessions;
        this.voiceConnector = new VoiceConnector(sessions);
        this.prefix = prefix;
    }

    public void handle(MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();
        if (!content.startsWith(prefix))
            return;

        String[] parts = content.substring(prefix.length()).trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty())
            return;

        String cmdName = parts[0].toLowerCase();
        Command command = registry.get(cmdName);
        if (command == null)
            return;

        if (requiresVoice(cmdName)) {
            Member member = event.getMember();
            if (member == null || !voiceConnector.ensureConnected(event.getGuild(), member.getIdLong())) {
                event.getChannel().sendMessage("Join a voice channel first").queue();
                return;
            }
        }
        ArgParser parsed = new ArgParser(java.util.Arrays.copyOfRange(parts, 1, parts.length));
        CommandContext ctx = new CommandContext(event.getGuild().getIdLong(),
                sessions.get(event.getGuild().getIdLong()), parsed.positional(), parsed.named(),
                (String msg) -> event.getChannel().sendMessage(java.util.Objects.requireNonNull(msg)).queue(),
                (String err) -> event.getChannel().sendMessage(java.util.Objects.requireNonNull(err)).queue(),
                (Object payload) -> {
                    String message = payload.toString();
                    if (message.isBlank() || message.isEmpty()) {
                        log.warn("Payload for the command {} is empty", cmdName);
                    } else
                        event.getChannel().sendMessage(message).queue();
                });
        try {
            command.execute(ctx);
        } catch (Exception e) {
            log.error("Command '{}' threw an exception", cmdName, e);
            event.getChannel().sendMessage("Something went wrong with a command").queue();
        }
    }

    private boolean requiresVoice(String cmdName) {
        return true;
    }
}