package discord.commands.player;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import discord.commands.Command;
import discord.commands.CommandContext;

@Component
public class ReorderCommand implements Command {
    private static final Gson gson = new Gson();

    @Override
    public String name() {
        return "reorder";
    }

    @Override
    public void execute(CommandContext ctx) {
        String raw = ctx.named("trackIds");
        if (raw == null) {
            ctx.error("Missing trackIds");
            return;
        }
        List<String> trackIds;
        try {
            trackIds = gson.fromJson(raw, new TypeToken<List<String>>() {
            }.getType());
        } catch (Exception e) {
            ctx.error("Invalid tracksIds payload");
            return;
        }
        System.out.println("raw=" + raw + " parsed=" + trackIds);
        ctx.musicManager().reorder(trackIds);
    }
}
