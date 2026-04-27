package discord.commands;

import discord.db.DatabaseManager;
import discord.util.CommandParser.ParsedCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.List;
import java.util.Map;

public class PoolList extends BaseCommand {

    private static final int PAGE_SIZE = 10;

    @Override @Nonnull public String getName()        { return "poollist"; }
    @Override @Nonnull public String getDescription() { return "Lists all images in a pool."; }
    @Override @Nonnull public String getUsage()       { return "`!poollist <n> [page]`"; }

    @Override
    public void execute(MessageReceivedEvent event, ParsedCommand cmd) {
        if (cmd.args.isEmpty()) {
            event.getChannel().sendMessage("❌ Usage: " + getUsage()).queue();
            return;
        }

        String name = cmd.args.get(0);
        int page = 1;
        if (cmd.args.size() >= 2) {
            try { page = Math.max(1, Integer.parseInt(cmd.args.get(1))); }
            catch (NumberFormatException ignored) {}
        }

        List<Map<String, Object>> poolRows = DatabaseManager.select(
                "image_pool",
                new String[]{"name"},
                new Object[]{name}
        );
        if (poolRows.isEmpty()) {
            event.getChannel().sendMessage("❌ No pool named `" + name + "`.").queue();
            return;
        }

        Map<String, Object> pool = poolRows.get(0);
        double bias = toDouble(pool.get("bias"), 1.0);

        List<Map<String, Object>> images = DatabaseManager.selectOrdered(
                "pool_image",
                new String[]{"pool_name"},
                new Object[]{name},
                "added_order",
                true
        );

        if (images.isEmpty()) {
            event.getChannel().sendMessage("ℹ️ Pool **" + name + "** is empty. Add images with `!pooladd`.").queue();
            return;
        }

        int total     = images.size();
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        page = Math.min(page, totalPages);

        int fromIdx = (page - 1) * PAGE_SIZE;
        int toIdx   = Math.min(fromIdx + PAGE_SIZE, total);

        StringBuilder sb = new StringBuilder();
        for (int i = fromIdx; i < toIdx; i++) {
            Map<String, Object> img = images.get(i);
            int slot = toInt(img.get("added_order"));
            String url = (String) img.get("url");
            // Truncate long URLs for readability
            String display = url.length() > 60 ? url.substring(0, 57) + "..." : url;
            sb.append(String.format("`#%d` %s\n", slot, display));
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🖼️ Pool: " + name)
                .setDescription(sb.toString())
                .addField("Images", String.valueOf(total), true)
                .addField("Bias", String.format("%.2f", bias), true)
                .setFooter("Page " + page + " of " + totalPages + " · Slot #" + (fromIdx + 1) + "–#" + toIdx)
                .setColor(Color.CYAN);

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    private double toDouble(Object v, double fallback) {
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return fallback; }
    }

    private int toInt(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }
}