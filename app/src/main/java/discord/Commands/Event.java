package discord.commands;

import discord.db.DatabaseManager;
import discord.util.DateUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import discord.util.CommandParser.ParsedCommand;

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

    /** Reusable — called both from command and from EventScheduler. */
    public static void sendEventEmbed(net.dv8tion.jda.api.entities.channel.middleman.MessageChannel channel,
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
        if (now.isBefore(dateFrom))   color = Color.YELLOW; // not started
        else if (now.isAfter(dateTo)) color = Color.GRAY;   // ended
        else                          color = Color.GREEN;   // ongoing

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📅 " + entry.get("name"))
                .addField("Started", entry.get("date_from") + " — " + elapsed,   false)
                .addField("Ends",    entry.get("date_to")   + " — " + countdown, false)
                .setColor(color);

        String imageUrl = (String) entry.get("image_url");

        // Send embed first, then image as a separate message
        channel.sendMessageEmbeds(embed.build()).queue(msg ->
                channel.sendMessage(imageUrl).queue()
        );
    }
}