package discord.commands;

import discord.db.DatabaseManager;
import discord.util.CommandParser.ParsedCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class PoolCreate extends BaseCommand {

    @Override @Nonnull public String getName()        { return "poolcreate"; }
    @Override @Nonnull public String getDescription() { return "Creates a new image pool with an optional bias (0.0–2.0)."; }
    @Override @Nonnull public String getUsage()       { return "`!poolcreate <name> [bias]`  — bias default 1.0"; }

    @Override
    public void execute(MessageReceivedEvent event, ParsedCommand cmd) {
        if (cmd.args.isEmpty()) {
            event.getChannel().sendMessage("❌ Usage: " + getUsage()).queue();
            return;
        }

        String name    = cmd.args.get(0);
        String ownerId = event.getAuthor().getId();
        double bias    = 1.0;

        if (cmd.args.size() >= 2) {
            try {
                bias = Double.parseDouble(cmd.args.get(1));
                if (bias < 0.0 || bias > 10.0) {
                    event.getChannel().sendMessage("❌ Bias must be between `0.0` and `10.0`.").queue();
                    return;
                }
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("❌ Bias must be a decimal number, e.g. `0.5` or `2.0`.").queue();
                return;
            }
        }

        boolean exists = DatabaseManager.exists(
                "image_pool",
                new String[]{"name"},
                new Object[]{name}
        );
        if (exists) {
            event.getChannel().sendMessage("❌ A pool named `" + name + "` already exists.").queue();
            return;
        }

        DatabaseManager.insert(
                "image_pool",
                new String[]{"name", "owner_id", "bias"},
                new Object[]{name, ownerId, bias}
        );

        event.getChannel().sendMessage(
                "✅ Pool **" + name + "** created with bias `" + bias + "`. " +
                "Add images with `!pooladd " + name + " <url>`."
        ).queue();
    }
}