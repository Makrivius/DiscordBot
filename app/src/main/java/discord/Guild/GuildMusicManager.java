package discord.guild;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import discord.audioPlayer.AudioPlayerSendHandler;
import discord.audioPlayer.PlayerStateDTO;
import discord.audioPlayer.TrackScheduler;
import net.dv8tion.jda.api.managers.AudioManager;

public class GuildMusicManager {
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager apm) {
        player = apm.createPlayer();
        scheduler = new TrackScheduler(player);
        sendHandler = new AudioPlayerSendHandler(player);
        player.addListener(scheduler);
    }

    public void connect(AudioManager guildAudioManager) {
        guildAudioManager.setSendingHandler(sendHandler);
    }

    public PlayerStateDTO snapshot() {
        return scheduler.snapshot();
    }
}
