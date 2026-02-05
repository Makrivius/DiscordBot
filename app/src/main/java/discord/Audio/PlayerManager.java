package discord.audio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class PlayerManager {
    private static PlayerManager INSTANCE;

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();

    private PlayerManager() {
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public static synchronized PlayerManager get() {
        if (INSTANCE == null)
            INSTANCE = new PlayerManager();
        return INSTANCE;
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return musicManagers.computeIfAbsent(guild.getIdLong(), id -> {
            var manager = new GuildMusicManager(playerManager);
            guild.getAudioManager().setSendingHandler(manager.getSendHandler());
            return manager;
        });
    }

    public void loadAndPlay(MessageChannel channel, String trackUrl) {
        var guild = ((GuildChannel) channel).getGuild();
        var musicManager = getGuildMusicManager(guild);

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
                channel.sendMessage("Added to queue: " + track.getInfo().title).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();

                if (tracks.isEmpty()) {
                    channel.sendMessage("Playlist is empty.").queue();
                    return;
                }

                for (AudioTrack track : tracks) {
                    musicManager.scheduler.queue(track);
                }

                channel.sendMessage("Loaded playlist: " + playlist.getName() +
                        " (" + tracks.size() + " tracks)").queue();
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found.").queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                channel.sendMessage("Could not play: " + e.getMessage()).queue();
            }
        });
    }
}

