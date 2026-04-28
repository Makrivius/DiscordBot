package discord.commands;

import discord.db.DatabaseManager;
import discord.handlers.ConfirmationHandler;
import discord.util.CommandParser.ParsedCommand;
import discord.util.DateUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class EventConfig extends BaseCommand {

    @Override @Nonnull public String getName()        { return "eventconfig"; }
    @Override @Nonnull public String getDescription() { return "Configures an auto-posting event linked to an image pool."; }
    @Override @Nonnull public String getUsage()       { return "`!eventconfig <name> <#channel> <pool_name> <date_from yyyy-MM-dd> <date_to yyyy-MM-dd> [bias]`  — bias default 1.0"; }

    @Override
    public void execute(MessageReceivedEvent event, ParsedCommand cmd) {
        if (cmd.args.size() < 5) {
            event.getChannel().sendMessage("❌ Usage: " + getUsage()).queue();
            return;
        }

        String name      = cmd.args.get(0);
        String channelId = cmd.args.get(1).replaceAll("[^0-9]", "");
        String poolName  = cmd.args.get(2);
        String rawFrom   = cmd.args.get(3);
        String rawTo     = cmd.args.get(4);
        String ownerId   = event.getAuthor().getId();
        double bias      = 1.0;

        if (cmd.args.size() >= 6) {
            try {
                bias = Double.parseDouble(cmd.args.get(5));
                if (bias < 0.0 || bias > 10.0) {
                    event.getChannel().sendMessage("❌ Bias must be between `0.0` and `10.0`.").queue();
                    return;
                }
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("❌ Bias must be a decimal number, e.g. `0.5` or `2.0`.").queue();
                return;
            }
        }

        if (!DatabaseManager.exists("image_pool", new String[]{"name"}, new Object[]{poolName})) {
            event.getChannel().sendMessage(
                    "❌ No pool named `" + poolName + "`. Create it first with `!poolcreate`."
            ).queue();
            return;
        }

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

        boolean exists = DatabaseManager.exists("command_config", new String[]{"name"}, new Object[]{name});

        if (exists) {
            List<Map<String, Object>> rows = DatabaseManager.select(
                    "command_config", new String[]{"name"}, new Object[]{name}
            );
            Map<String, Object> current = rows.get(0);
            String entryOwnerId = (String) current.get("owner_id");

            String description = String.format(
                    "An entry for **%s** already exists:\n\n" +
                    "**Channel:** <#%s> → <#%s>\n" +
                    "**Pool:** %s → %s\n" +
                    "**From:** %s → %s\n" +
                    "**To:** %s → %s\n" +
                    "**Bias:** %s → %.1f\n\n" +
                    "Do you want to overwrite it?",
                    name,
                    current.get("channel_id"), channelId,
                    current.get("pool_name"),  poolName,
                    current.get("date_from"),  rawFrom,
                    current.get("date_to"),    rawTo,
                    current.get("bias"),       bias
            );

            double finalBias = bias;
            ConfirmationHandler.requestWithPermission(event, entryOwnerId, description, () ->
                    DatabaseManager.upsert(
                            "command_config",
                            new String[]{"name", "channel_id", "pool_name", "date_from", "date_to", "owner_id", "bias"},
                            new Object[]{name, channelId, poolName, rawFrom, rawTo, ownerId, finalBias}
                    )
            );
        } else {
            DatabaseManager.insert(
                    "command_config",
                    new String[]{"name", "channel_id", "pool_name", "date_from", "date_to", "owner_id", "bias"},
                    new Object[]{name, channelId, poolName, rawFrom, rawTo, ownerId, bias}
            );
            event.getChannel().sendMessage(
                    "✅ Event **" + name + "** configured with pool `" + poolName +
                    "` and bias `" + bias + "`. Daily posts start on `" + rawFrom + "`."
            ).queue();
        }
    }
}