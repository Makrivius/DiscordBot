package discord.commands;

import java.util.List;

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
        var guild = event.getGuild();
        var member = event.getMember();

        if (member == null) {
            event.getChannel().sendMessage("This command can only be used in a server.").queue();
            return;
        }

        // Parse command and args
        var parsed = tools.CommandParser.parse(event.getMessage().getContentRaw());
        if (parsed == null) {
            return;
        }
        // Check for "shuffle" flag
        List<String> args = parsed.args;
        boolean shuffle = false;
        if (!args.isEmpty()) {
            String last = args.get(args.size() - 1).toLowerCase();
            if (last.equals("shuffle") || last.equals("true")) {
                shuffle = true;
                args.remove(args.size() - 1);
            }
        }
        // Join remaining args into a search query or URL
        String query = String.join(" ", args);

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
