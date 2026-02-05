package discord.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord.handlers.AudioPlayerSendHandler;
import discord.ui.NowPlaying;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import tools.TrackScheduler;

public class GuildMusicManager {

    public final AudioPlayer player;
    public final TrackScheduler scheduler;

    private final AudioPlayerSendHandler sendHandler;
    private Message nowPlayingMessage;
    private TextChannel currentChannel;

    public GuildMusicManager(AudioPlayerManager manager) {
        this.player = manager.createPlayer();
        this.scheduler = new TrackScheduler(player);

        this.player.addListener(scheduler);

        // Set up track start listener
        scheduler.setTrackStartListener(track -> {
            if (currentChannel != null) {
                sendOrUpdateNowPlaying(currentChannel, track);
            }
        });

        this.sendHandler = new AudioPlayerSendHandler(player);
    }

    public void setCurrentChannel(TextChannel channel) {
        this.currentChannel = channel;
    }

    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }

    public void sendOrUpdateNowPlaying(TextChannel channel, AudioTrack track) {
    var music = this; // GuildMusicManager

    var embed = NowPlaying.build(track);
    var components = NowPlaying.buttons();

    if (music.nowPlayingMessage != null) {
        music.nowPlayingMessage.editMessageEmbeds(embed)
            .setComponents(components)
            .queue(
                success -> {},
                failure -> {
                    // If edit fails (deleted, no perms, etc.)
                    channel.sendMessageEmbeds(embed)
                        .setComponents(components)
                        .queue(msg -> music.nowPlayingMessage = msg);
                }
            );
    } else {
        channel.sendMessageEmbeds(embed)
            .setComponents(components)
            .queue(msg -> music.nowPlayingMessage = msg);
    }
}

}

