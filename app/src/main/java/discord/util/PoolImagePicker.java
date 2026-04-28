package discord.util;

import discord.db.DatabaseManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Picks a random image from a named pool using exponential bias weighting.
 * Bias is stored on the event (command_config), not the pool itself.
 *
 * Images are ordered by added_order ascending (0 = oldest, n-1 = newest).
 * Weight for image at index i = e^(bias * i / (n-1))
 *
 * bias = 0.0 → uniform (all equal chance)
 * bias = 1.0 → newest ~2.7× more likely than oldest
 * bias = 2.0 → newest ~7.4× more likely than oldest
 */
public class PoolImagePicker {

    private static final Random RANDOM = new Random();

    /**
     * Picks a URL from the given pool using the provided bias value.
     * Returns null if the pool doesn't exist or is empty.
     */
    public static String pick(String poolName, double bias) {
        List<Map<String, Object>> images = DatabaseManager.selectOrdered(
                "pool_image",
                new String[]{"pool_name"},
                new Object[]{poolName},
                "added_order",
                true
        );

        if (images == null || images.isEmpty()) return null;
        if (images.size() == 1) return (String) images.get(0).get("url");

        int n = images.size();
        double[] weights = new double[n];
        double total = 0.0;

        for (int i = 0; i < n; i++) {
            double normIndex = (double) i / (n - 1);
            weights[i] = Math.exp(bias * normIndex);
            total += weights[i];
        }

        double roll = RANDOM.nextDouble() * total;
        double cumulative = 0.0;
        for (int i = 0; i < n; i++) {
            cumulative += weights[i];
            if (roll <= cumulative) return (String) images.get(i).get("url");
        }

        return (String) images.get(n - 1).get("url");
    }
}