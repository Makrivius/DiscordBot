package discord.commands;

import discord.db.DatabaseManager;
import discord.util.CommandParser.ParsedCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class PoolCreate extends BaseCommand {

    @Override @Nonnull public String getName()        { return "poolcreate"; }
    @Override @Nonnull public String getDescription() { return "Creates a new image pool."; }
    @Override @Nonnull public String getUsage()       { return "`!poolcreate <name>`"; }

    @Override
    public void execute(MessageReceivedEvent event, ParsedCommand cmd) {
        if (cmd.args.isEmpty()) {
            event.getChannel().sendMessage("❌ Usage: " + getUsage()).queue();
            return;
        }

        String name    = cmd.args.get(0);
        String ownerId = event.getAuthor().getId();

        if (DatabaseManager.exists("image_pool", new String[]{"name"}, new Object[]{name})) {
            event.getChannel().sendMessage("❌ A pool named `" + name + "` already exists.").queue();
            return;
        }

        DatabaseManager.insert(
                "image_pool",
                new String[]{"name", "owner_id"},
                new Object[]{name, ownerId}
        );

        event.getChannel().sendMessage(
                "✅ Pool **" + name + "** created. Add images with `!pooladd " + name + " <url>`."
        ).queue();
    }
}