package discord.guild;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord.audioPlayer.TrackScheduler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

public class VoiceConnector {
    private static final Logger log = LoggerFactory.getLogger(VoiceConnector.class);

    private final SessionRegistry sessions;

    public VoiceConnector(SessionRegistry sessions) {
        this.sessions = sessions;
    }

    /**
     * Ensures that bot is connected to the voice channel that {@code memberId} is
     * currently in, for given guild.
     * 
     * @return true if connected now or already, false if member isn't in voice
     *         channel.
     */
    public boolean ensureConnected(Guild guild, long memberId) {
        Member member = guild.getMemberById(memberId);
        if (member == null) {
            log.warn("Could not resolve member {} in guild {}", memberId, guild.getIdLong());
            return false;
        }

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || !voiceState.inAudioChannel()) {
            return false;
        }

        AudioChannel audioChannel = voiceState.getChannel();
        if (audioChannel == null) {
            log.error("Cannot get audioChannel info!");
            return false;
        }

        TrackScheduler scheduler = sessions.getScheduler(guild.getIdLong());
        scheduler.setChannelName(audioChannel.getName());

        guild.getJDA().getDirectAudioController().connect(audioChannel);
        return true;
    }
}
