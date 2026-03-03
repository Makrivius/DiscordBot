package discord.commands;

import discord.audio.PlayerManager;
import discord.util.CommandParser.ParsedCommand;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Play extends BaseCommand {

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getDescription() {
        return "Plays a song from YouTube or a direct URL. Use --shuffle to shuffle playlist results.";
    }

    @Override
    public String getUsage() {
        return "!play [--shuffle] <URL or search query or playlist URL>";
    }

    @Override
    public void execute(MessageReceivedEvent event, ParsedCommand cmd) {
        // Check for "shuffle" flag
        boolean shuffle = cmd.args.remove("--shuffle") || cmd.args.remove("-s");

        // Join remaining args into a search query or URL
        String query = String.join(" ", cmd.args);

        var guild = event.getGuild();
        var member = event.getMember();

        if (member == null) {
            event.getChannel().sendMessage("This command can only be used in a server.").queue();
            return;
        }

        if (query.isBlank()) {
            event.getChannel().sendMessage("❌ Provide a URL or search query.").queue();
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

        PlayerManager.get().loadAndPlay(event.getChannel(), query, shuffle);
    }

}
