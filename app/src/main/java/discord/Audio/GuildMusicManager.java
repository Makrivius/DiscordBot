package discord.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import discord.handlers.AudioPlayerSendHandler;
import tools.TrackScheduler;

public class GuildMusicManager {

    public final AudioPlayer player;
    public final TrackScheduler scheduler;

    private final AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager manager) {
        this.player = manager.createPlayer();
        this.scheduler = new TrackScheduler(player);

        this.player.addListener(scheduler);

        this.sendHandler = new AudioPlayerSendHandler(player);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }
}

