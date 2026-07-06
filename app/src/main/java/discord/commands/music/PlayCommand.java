package discord.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord.commands.Command;
import discord.guild.GuildMusicManager;
import discord.guild.SessionRegistry;
import discord.util.ArgParser;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class PlayCommand implements Command {
    private final SessionRegistry sessions;

    public PlayCommand(SessionRegistry sessions) {
        this.sessions = sessions;
    }

    @Override
    public String name() {
        return "play";
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        ArgParser parsed = new ArgParser(args);
        if (parsed.positional().isEmpty()) {
            event.getChannel().sendMessage("Usage `!play <query|url> [--shuffle]`");
            return;
        }
        if (!ensureConnected(event)) {
            event.getChannel().sendMessage("Join a voice channel first").queue();
            return;
        }
        String query = parsed.joinedQuery();
        boolean shuffle = parsed.has("shuffle");

        GuildMusicManager manager = sessions.get(event.getGuild().getIdLong());

        manager.loadAndQueue(query, shuffle, result -> {
            if (result instanceof AudioTrack track) {
                event.getChannel().sendMessage("Queued: **" + track.getInfo().title + "**").queue();
            } else if (result instanceof AudioPlaylist playlist) {
                event.getChannel().sendMessage("Queued playlist: **" + playlist.getName() + "** ("
                        + playlist.getTracks().size() + " tracks" + (shuffle ? ", shuffled" : "") + ")").queue();

            }
        }, error -> event.getChannel().sendMessage(error.isEmpty() ? "Unknown error" : error).queue());
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
        manager.connect(audioManager);

        AudioChannel audioChannel = voiceState.getChannel();
        if (audioChannel != null)
            audioManager.openAudioConnection(audioChannel);
        return true;
    }
}
