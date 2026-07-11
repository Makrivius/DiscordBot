package discord.commands;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord.guild.GuildMusicManager;
import discord.guild.SessionRegistry;
import discord.util.ArgParser;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class DiscordCommandDispatcher {
    private static final Logger log = LoggerFactory.getLogger(DiscordCommandDispatcher.class);
    private final CommandRegistry registry;
    private final SessionRegistry sessions;
    private final String prefix;

    public DiscordCommandDispatcher(CommandRegistry registry,
            SessionRegistry sessions,
            String prefix) {
        this.registry = registry;
        this.sessions = sessions;
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
        if (requiresVoice(cmdName) && !ensureConnected(event)) {
            event.getChannel().sendMessage("Join a voice channel first").queue();
            return;
        }
        ArgParser parsed = new ArgParser(java.util.Arrays.copyOfRange(parts, 1, parts.length));
        CommandContext ctx = new CommandContext(event.getGuild().getIdLong(),
                sessions.get(event.getGuild().getIdLong()), parsed.positional(), parsed.named(),
                (@Nonnull String msg) -> event.getChannel().sendMessage(msg).queue(),
                (@Nonnull String err) -> event.getChannel().sendMessage(err).queue(),
                (@Nonnull Object payload) -> event.getChannel().sendMessage(payload.toString()).queue());
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

    private boolean ensureConnected(MessageReceivedEvent event) {
        AudioManager audioManager = event.getGuild().getAudioManager();
        if (audioManager.isConnected())
            return true;

        Member member = event.getMember();
        if (member == null)
            return false;

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || !voiceState.inAudioChannel()) {
            return false;
        }

        GuildMusicManager manager = sessions.get(event.getGuild().getIdLong());

        AudioChannel audioChannel = voiceState.getChannel();
        if (audioChannel == null) {
            log.error("Cannot get audioChannel info!");
            return false;
        }
        manager.connect(audioManager, audioChannel.getName());

        if (audioChannel != null)
            audioManager.openAudioConnection(audioChannel);
        return true;
    }

}