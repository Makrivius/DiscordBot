package discord.commands;

import discord.db.DatabaseManager;
import discord.util.CommandParser.ParsedCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class PoolAdd extends BaseCommand {

    @Override @Nonnull public String getName()        { return "pooladd"; }
    @Override @Nonnull public String getDescription() { return "Adds an image URL to a pool."; }
    @Override @Nonnull public String getUsage()       { return "`!pooladd <n> <image_url>`"; }

    @Override
    public void execute(MessageReceivedEvent event, ParsedCommand cmd) {
        if (cmd.args.size() < 2) {
            event.getChannel().sendMessage("❌ Usage: " + getUsage()).queue();
            return;
        }

        String name = cmd.args.get(0);
        String url  = cmd.args.get(1);

        if (!DatabaseManager.exists("image_pool", new String[]{"name"}, new Object[]{name})) {
            event.getChannel().sendMessage("❌ No pool named `" + name + "`. Create it first with `!poolcreate`.").queue();
            return;
        }

        // Determine the next added_order value
        List<Map<String, Object>> existing = DatabaseManager.selectOrdered(
                "pool_image",
                new String[]{"pool_name"},
                new Object[]{name},
                "added_order",
                false  // descending — we want the max
        );
        int nextOrder = existing.isEmpty() ? 1 : toInt(existing.get(0).get("added_order")) + 1;

        DatabaseManager.insert(
                "pool_image",
                new String[]{"pool_name", "url", "added_order"},
                new Object[]{name, url, nextOrder}
        );

        event.getChannel().sendMessage(
                "✅ Image added to **" + name + "** (slot #" + nextOrder + ")."
        ).queue();
    }

    private int toInt(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }
}