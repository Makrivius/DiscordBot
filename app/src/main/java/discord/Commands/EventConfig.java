package discord.commands;

import discord.db.DatabaseManager;
import discord.util.CommandParser.ParsedCommand;
import discord.util.ConfirmationHandler;
import discord.util.DateUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class EventConfig extends BaseCommand {

    @Override @Nonnull public String getName()        { return "eventconfig"; }
    @Override @Nonnull public String getDescription() { return "Configures an auto-posting event with an image."; }
    @Override @Nonnull public String getUsage()       { return "`!eventconfig <name> <#channel> <image_url> <date_from yyyy-MM-dd> <date_to yyyy-MM-dd>`"; }

    @Override
    public void execute(MessageReceivedEvent event, ParsedCommand cmd) {
        if (cmd.args.size() < 5) {
            event.getChannel().sendMessage("❌ Usage: " + getUsage()).queue();
            return;
        }

        String name      = cmd.args.get(0);
        String channelId = cmd.args.get(1).replaceAll("[^0-9]", ""); // strip <#...> mention if used
        String imageUrl  = cmd.args.get(2);
        String rawFrom   = cmd.args.get(3);
        String rawTo     = cmd.args.get(4);
        String ownerId   = event.getAuthor().getId();

        LocalDate dateFrom = DateUtil.parse(rawFrom);
        LocalDate dateTo   = DateUtil.parse(rawTo);

        if (dateFrom == null || dateTo == null) {
            event.getChannel().sendMessage("❌ Invalid date format. Use `yyyy-MM-dd` (e.g. `2025-03-01`).").queue();
            return;
        }
        if (!dateTo.isAfter(dateFrom)) {
            event.getChannel().sendMessage("❌ `date_to` must be after `date_from`.").queue();
            return;
        }

        boolean exists = DatabaseManager.exists(
                "command_config",
                new String[]{"name"},
                new Object[]{name}
        );

        if (exists) {
            List<Map<String, Object>> rows = DatabaseManager.select(
                    "command_config",
                    new String[]{"name"},
                    new Object[]{name}
            );
            Map<String, Object> current = rows.get(0);
            String entryOwnerId = (String) current.get("owner_id");

            String description = String.format(
                    "An entry for **%s** already exists:\n\n" +
                    "**Channel:** <#%s> → <#%s>\n" +
                    "**Image:** %s → %s\n" +
                    "**From:** %s → %s\n" +
                    "**To:** %s → %s\n\n" +
                    "Do you want to overwrite it?",
                    name,
                    current.get("channel_id"), channelId,
                    current.get("image_url"),  imageUrl,
                    current.get("date_from"),  rawFrom,
                    current.get("date_to"),    rawTo
            );

            ConfirmationHandler.requestWithPermission(event, entryOwnerId, description, () ->
                    DatabaseManager.upsert(
                            "command_config",
                            new String[]{"name", "channel_id", "image_url", "date_from", "date_to", "owner_id"},
                            new Object[]{name, channelId, imageUrl, rawFrom, rawTo, ownerId}
                    )
            );
        } else {
            DatabaseManager.insert(
                    "command_config",
                    new String[]{"name", "channel_id", "image_url", "date_from", "date_to", "owner_id"},
                    new Object[]{name, channelId, imageUrl, rawFrom, rawTo, ownerId}
            );
            event.getChannel().sendMessage("✅ Event **" + name + "** configured. Daily posts will start on `" + rawFrom + "`.").queue();
        }
    }
}