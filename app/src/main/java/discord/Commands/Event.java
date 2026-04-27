package discord.commands;

import discord.db.DatabaseManager;
import discord.util.CommandParser.ParsedCommand;
import discord.util.DateUtil;
import discord.util.PoolImagePicker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class Event extends BaseCommand {

    @Override @Nonnull public String getName()        { return "event"; }
    @Override @Nonnull public String getDescription() { return "Shows current event info and time status."; }
    @Override @Nonnull public String getUsage()       { return "`!event <n>`"; }

    @Override
    public void execute(MessageReceivedEvent event, ParsedCommand cmd) {
        if (cmd.args.isEmpty()) {
            event.getChannel().sendMessage("❌ Usage: " + getUsage()).queue();
            return;
        }

        String name = cmd.args.get(0);
        List<Map<String, Object>> rows = DatabaseManager.select(
                "command_config",
                new String[]{"name"},
                new Object[]{name}
        );

        if (rows.isEmpty()) {
            event.getChannel().sendMessage("❌ No event found with name `" + name + "`.").queue();
            return;
        }

        sendEventEmbed(event.getChannel(), rows.get(0));
    }

    /**
     * Reusable — called both from this command and from EventScheduler.
     *
     * Picks a random image from the event's pool using the pool's bias, then
     * sends the embed followed by the picked image URL.
     */
    public static void sendEventEmbed(
            net.dv8tion.jda.api.entities.channel.middleman.MessageChannel channel,
            Map<String, Object> entry) {

        LocalDate dateFrom = DateUtil.parse((String) entry.get("date_from"));
        LocalDate dateTo   = DateUtil.parse((String) entry.get("date_to"));
        if (dateFrom == null || dateTo == null) {
            channel.sendMessage("❌ Event has invalid dates stored.").queue();
            return;
        }

        String elapsed   = DateUtil.elapsed(dateFrom);
        String countdown = DateUtil.countdown(dateTo);

        LocalDate now = LocalDate.now();
        Color color;
        if      (now.isBefore(dateFrom)) color = Color.YELLOW;
        else if (now.isAfter(dateTo))    color = Color.GRAY;
        else                             color = Color.GREEN;

        // Pick an image from the pool
        String poolName = (String) entry.get("pool_name");
        String imageUrl = PoolImagePicker.pick(poolName);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📅 " + entry.get("name"))
                .addField("Started", entry.get("date_from") + " — " + elapsed,   false)
                .addField("Ends",    entry.get("date_to")   + " — " + countdown, false)
                .addField("Pool",    poolName, true)
                .setColor(color);

        if (imageUrl != null) {
            // Send embed first, then image as a separate message (matching original behaviour)
            channel.sendMessageEmbeds(embed.build()).queue(msg ->
                    channel.sendMessage(imageUrl).queue()
            );
        } else {
            // Pool is empty — send embed with a warning, no crash
            embed.setFooter("⚠️ Pool \"" + poolName + "\" is empty — no image today.");
            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }
}