package discord.audio;

import java.util.HashMap;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class PlayerManager {
    private static PlayerManager INSTANCE;

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();

    private PlayerManager() {
        this.playerManager = new DefaultAudioPlayerManager();

        // Create YouTube source with OAuth
        YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager();

        // // Load refresh token from .env (or trigger OAuth flow)
        // String refresh = Config.YT_REFRESH_TOKEN;

        // if (refresh == null || refresh.isBlank()) {
        // // First-time OAuth flow
        // youtube.useOauth2(null, false);
        // } else {
        // // Use stored refresh token
        // youtube.useOauth2(refresh, true);
        // }

        playerManager.registerSourceManager(youtube);

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
                if (playlist.isSearchResult()) {
                    // Only first result for searches
                    AudioTrack first = playlist.getTracks().get(0);
                    musicManager.scheduler.queue(first);
                    channel.sendMessage("Added top result: " + first.getInfo().title).queue();
                    return;
                }

                // Real playlist URL → queue all tracks
                for (AudioTrack track : playlist.getTracks()) {
                    musicManager.scheduler.queue(track);
                }

                channel.sendMessage("Loaded playlist: " + playlist.getName() +
                        " (" + playlist.getTracks().size() + " tracks)").queue();
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
