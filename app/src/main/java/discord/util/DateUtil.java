package discord.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Parses an ISO date string. Returns null if invalid.
     * Accepts: 2025-03-01
     */
    public static LocalDate parse(String raw) {
        try {
            return LocalDate.parse(raw.trim(), FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Human-readable elapsed time since a past date.
     * e.g. "2 months, 15 days ago"
     */
    public static String elapsed(LocalDate from) {
        LocalDate now = LocalDate.now();
        if (!from.isBefore(now)) return "Not started yet";

        long totalDays = ChronoUnit.DAYS.between(from, now);
        return formatDuration(totalDays) + " ago";
    }

    /**
     * Human-readable countdown to a future date.
     * e.g. "in 7 days" or "Ended X ago"
     */
    public static String countdown(LocalDate to) {
        LocalDate now = LocalDate.now();
        if (to.isBefore(now)) {
            long totalDays = ChronoUnit.DAYS.between(to, now);
            return "Ended " + formatDuration(totalDays) + " ago";
        }
        if (to.isEqual(now)) return "Ends today!";

        long totalDays = ChronoUnit.DAYS.between(now, to);
        return "in " + formatDuration(totalDays);
    }

    /**
     * Converts total days into "X years, X months, X days" — skipping zero units.
     */
    public static String formatDuration(long totalDays) {
        long years  = totalDays / 365;
        long months = (totalDays % 365) / 30;
        long days   = (totalDays % 365) % 30;

        StringBuilder sb = new StringBuilder();
        if (years  > 0) sb.append(years).append(years  == 1 ? " year"  : " years").append(", ");
        if (months > 0) sb.append(months).append(months == 1 ? " month" : " months").append(", ");
        if (days   > 0) sb.append(days).append(days    == 1 ? " day"   : " days");

        // Trim trailing comma if days was 0
        String result = sb.toString().replaceAll(",\\s*$", "");
        return result.isEmpty() ? "0 days" : result;
    }
}