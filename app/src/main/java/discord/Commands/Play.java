package discord.commands;

import discord.audio.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Play extends BaseCommand {

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        String raw = event.getMessage().getContentRaw();
        String[] parts = raw.split(" ", 2);

        if (parts.length < 2) {
            event.getChannel().sendMessage("Usage: !play <url or search query>").queue();
            return;
        }

        String query = parts[1];
        var guild = event.getGuild();
        var member = event.getMember();

        if (member == null) {
            event.getChannel().sendMessage("This command can only be used in a server.").queue();
            return;
        }

        GuildVoiceState vs = member.getVoiceState();
        if (vs == null || !vs.inAudioChannel()) {
            event.getChannel().sendMessage("You must be in a voice channel!").queue();
            return;
        }

        var channel = vs.getChannel();
        var audioManager = guild.getAudioManager();

        // Auto-join if not already connected
        if (!audioManager.isConnected() && channel != null) {
            audioManager.openAudioConnection(channel);
        }

        // If user typed text instead of URL → treat as YouTube search
        if (!query.startsWith("http")) {
            query = "ytsearch:" + query;
        }

        PlayerManager.get().loadAndPlay(event.getChannel(), query);
    }
}
