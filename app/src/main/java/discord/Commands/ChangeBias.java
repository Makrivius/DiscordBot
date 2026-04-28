package discord.commands;

import discord.db.DatabaseManager;
import discord.util.CommandParser.ParsedCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class ChangeBias extends BaseCommand {

    @Override @Nonnull public String getName()        { return "changebias"; }
    @Override @Nonnull public String getDescription() { return "Changes the image pick bias for an event (0.0 = uniform, 1.0 = prefer newer, 2.0+ = strongly prefer newest)."; }
    @Override @Nonnull public String getUsage()       { return "`!changebias <event_name> <bias>`"; }

    @Override
    public void execute(MessageReceivedEvent event, ParsedCommand cmd) {
        if (cmd.args.size() < 2) {
            event.getChannel().sendMessage("❌ Usage: " + getUsage()).queue();
            return;
        }

        String eventName = cmd.args.get(0);
        double bias;
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

        List<Map<String, Object>> rows = DatabaseManager.select(
                "command_config", new String[]{"name"}, new Object[]{eventName}
        );
        if (rows.isEmpty()) {
            event.getChannel().sendMessage("❌ No event found with name `" + eventName + "`.").queue();
            return;
        }

        String ownerId = (String) rows.get(0).get("owner_id");
        String callerId = event.getAuthor().getId();

        // Only the event owner can change bias
        if (!callerId.equals(ownerId)) {
            event.getChannel().sendMessage("❌ Only the event owner can change its bias.").queue();
            return;
        }

        double oldBias = toDouble(rows.get(0).get("bias"), 1.0);

        DatabaseManager.update(
                "command_config",
                new String[]{"bias"},
                new Object[]{bias},
                new String[]{"name"},
                new Object[]{eventName}
        );

        event.getChannel().sendMessage(String.format(
                "✅ Bias for **%s** updated: `%.1f` → `%.1f`\n" +
                "_(0.0 = uniform random · 1.0 = prefer newer · 2.0+ = strongly prefer newest)_",
                eventName, oldBias, bias
        )).queue();
    }

    private double toDouble(Object v, double fallback) {
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return fallback; }
    }
}