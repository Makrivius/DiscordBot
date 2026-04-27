package discord.commands;

import discord.db.DatabaseManager;
import discord.handlers.ConfirmationHandler;
import discord.util.CommandParser.ParsedCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class PoolDelete extends BaseCommand {

    @Override @Nonnull public String getName()        { return "pooldelete"; }
    @Override @Nonnull public String getDescription() { return "Permanently deletes a pool and all its images."; }
    @Override @Nonnull public String getUsage()       { return "`!pooldelete <n>`"; }

    @Override
    public void execute(MessageReceivedEvent event, ParsedCommand cmd) {
        if (cmd.args.isEmpty()) {
            event.getChannel().sendMessage("❌ Usage: " + getUsage()).queue();
            return;
        }

        String name = cmd.args.get(0);

        List<Map<String, Object>> poolRows = DatabaseManager.select(
                "image_pool",
                new String[]{"name"},
                new Object[]{name}
        );
        if (poolRows.isEmpty()) {
            event.getChannel().sendMessage("❌ No pool named `" + name + "`.").queue();
            return;
        }

        String ownerId = (String) poolRows.get(0).get("owner_id");

        // Count images so the confirmation message is informative
        List<Map<String, Object>> images = DatabaseManager.select(
                "pool_image",
                new String[]{"pool_name"},
                new Object[]{name}
        );
        int count = images.size();

        String description = String.format(
                "⚠️ Delete pool **%s** and all **%d** image(s) inside it? This cannot be undone.",
                name, count
        );

        ConfirmationHandler.requestWithPermission(event, ownerId, description, () -> {
            // Delete images first (FK integrity), then the pool itself
            DatabaseManager.delete("pool_image", new String[]{"pool_name"}, new Object[]{name});
            DatabaseManager.delete("image_pool", new String[]{"name"},      new Object[]{name});
            event.getChannel().sendMessage("🗑️ Pool **" + name + "** deleted.").queue();
        });
    }
}