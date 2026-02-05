package discord.ui;

import java.util.List;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord.enums.PlayerAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class NowPlaying {
    public static MessageEmbed build(AudioTrack track) {
        var info = track.getInfo();
        return new EmbedBuilder().setTitle("Now Playing").setDescription(info.title)
                .addField("Duration", formatTime(track.getDuration()), true)
                .addField("Author", info.author != null ? info.author : "Unknown", true)
                .build();
    }

    public static List<ActionRow> buttons() {
        return List
                .of(ActionRow.of(
                        Button.primary(PlayerAction.PREVIOUS.id != null ? PlayerAction.PREVIOUS.id : "previous", "⏮"),
                        Button.primary(PlayerAction.PAUSE.id != null ? PlayerAction.PAUSE.id : "pause", "⏯"),
                        Button.primary(PlayerAction.SKIP.id != null ? PlayerAction.SKIP.id : "skip", "⏭"),
                        Button.danger(PlayerAction.STOP.id != null ? PlayerAction.STOP.id : "stop", "⏹"),
                        Button.secondary(PlayerAction.LOOP.id != null ? PlayerAction.LOOP.id : "loop", "🔁")));
    }

    private static String formatTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = millis / (1000 * 60 * 60);
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}
