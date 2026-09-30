package discord.commands.music;

import org.springframework.stereotype.Component;

import dev.arbjerg.lavalink.client.player.Track;
import discord.commands.Command;
import discord.commands.CommandContext;

@Component
public class PlayNowCommand implements Command {
    @Override
    public String name() {
        return "playnow";
    }

    @Override
    public void execute(CommandContext ctx) {
        String trackId = ctx.named("trackId");
        if (trackId == null || trackId.isBlank()) {
            ctx.error("Missing trackId");
            return;
        }
        ctx.trackLoader().playNowById(trackId, obj -> {
            Track track = (Track) obj;
            ctx.reply("Playing now: **" + track.getInfo().getTitle() + "**");
        }, error -> ctx.error(error));
    }
}
