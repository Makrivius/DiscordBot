package discord.commands;

import discord.db.DatabaseManager;
import discord.handlers.ConfirmationHandler;
import discord.util.CommandParser.ParsedCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class PoolRemove extends BaseCommand {

    @Override @Nonnull public String getName()        { return "poolremove"; }
    @Override @Nonnull public String getDescription() { return "Removes an image from a pool by its slot number."; }
    @Override @Nonnull public String getUsage()       { return "`!poolremove <n> <slot#>`  — use `!poollist` to see slot numbers"; }

    @Override
    public void execute(MessageReceivedEvent event, ParsedCommand cmd) {
        if (cmd.args.size() < 2) {
            event.getChannel().sendMessage("❌ Usage: " + getUsage()).queue();
            return;
        }

        String name = cmd.args.get(0);
        int slot;
        try {
            slot = Integer.parseInt(cmd.args.get(1));
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("❌ Slot must be a number. Use `!poollist " + name + "` to see slots.").queue();
            return;
        }

        List<Map<String, Object>> rows = DatabaseManager.selectOrdered(
                "pool_image",
                new String[]{"pool_name", "added_order"},
                new Object[]{name, slot},
                "added_order",
                true
        );

        if (rows.isEmpty()) {
            event.getChannel().sendMessage("❌ No image at slot #" + slot + " in pool `" + name + "`.").queue();
            return;
        }

        String url = (String) rows.get(0).get("url");

        // Find pool owner for permission check
        List<Map<String, Object>> poolRows = DatabaseManager.select(
                "image_pool",
                new String[]{"name"},
                new Object[]{name}
        );
        String ownerId = poolRows.isEmpty() ? event.getAuthor().getId()
                                            : (String) poolRows.get(0).get("owner_id");

        String description = String.format(
                "Remove image at slot **#%d** from pool **%s**?\n`%s`",
                slot, name, url
        );

        ConfirmationHandler.requestWithPermission(event, ownerId, description, () ->
                DatabaseManager.delete(
                        "pool_image",
                        new String[]{"pool_name", "added_order"},
                        new Object[]{name, slot}
                )
        );
    }
}