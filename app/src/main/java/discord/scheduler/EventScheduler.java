package discord.scheduler;

import discord.commands.Event;
import discord.db.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EventScheduler {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final JDA jda;
    private final ZoneId zone;

    /**
     * @param jda  JDA instance to resolve channels
     * @param zone timezone for midnight (e.g. ZoneId.of("UTC") or "Europe/Warsaw")
     */
    public EventScheduler(JDA jda, ZoneId zone) {
        this.jda  = jda;
        this.zone = zone;
    }

    /** Call once after JDA is ready. Schedules the first tick then repeats every 24h. */
    public void start() {
        long delaySeconds = secondsUntilMidnight();
        scheduler.scheduleAtFixedRate(this::tick, delaySeconds, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    // ─────────────────────────────────────────────
    // Internal
    // ─────────────────────────────────────────────

    private void tick() {
        LocalDate today = LocalDate.now(zone);

        // Fetch all active events (date_from <= today <= date_to)
        List<Map<String, Object>> events = DatabaseManager.selectWhere(
                "command_config",
                "date_from <= ? AND date_to >= ?",
                new Object[]{today.toString(), today.toString()}
        );

        for (Map<String, Object> entry : events) {
            String channelId = (String) entry.get("channel_id");
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel == null) {
                System.err.println("EventScheduler: channel not found: " + channelId);
                continue;
            }
            Event.sendEventEmbed(channel, entry);
        }
    }

    private long secondsUntilMidnight() {
        ZonedDateTime now          = ZonedDateTime.now(zone);
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone);
        return nextMidnight.toEpochSecond() - now.toEpochSecond();
    }
}