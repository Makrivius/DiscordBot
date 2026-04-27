package discord.util;

import discord.db.DatabaseManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Picks a random image from a named pool using exponential bias weighting.
 *
 * Images are ordered by added_order ascending (index 0 = oldest, n-1 = newest).
 * Each image at index i gets weight = e^(bias * i / (n-1)).
 *
 * bias = 0.0 → uniform (all weights = 1.0)
 * bias = 1.0 → newest is e ≈ 2.7× more likely than oldest
 * bias = 2.0 → newest is e² ≈ 7.4× more likely than oldest
 */
public class PoolImagePicker {

    private static final Random RANDOM = new Random();

    /**
     * Picks a URL from the given pool using the pool's stored bias.
     * Returns null if the pool doesn't exist or is empty.
     */
    public static String pick(String poolName) {
        // Fetch pool metadata for bias value
        List<Map<String, Object>> poolRows = DatabaseManager.select(
                "image_pool",
                new String[]{"name"},
                new Object[]{poolName}
        );
        if (poolRows.isEmpty()) return null;

        double bias = toDouble(poolRows.get(0).get("bias"), 0.0);

        // Fetch images ordered oldest → newest
        List<Map<String, Object>> images = DatabaseManager.selectOrdered(
                "pool_image",
                new String[]{"pool_name"},
                new Object[]{poolName},
                "added_order",
                true
        );
        if (images.isEmpty()) return null;
        if (images.size() == 1) return (String) images.get(0).get("url");

        int n = images.size();
        double[] weights = new double[n];
        double total = 0.0;

        for (int i = 0; i < n; i++) {
            // Normalise index to [0, 1] so bias scale is independent of pool size
            double normIndex = (n == 1) ? 0.0 : (double) i / (n - 1);
            weights[i] = Math.exp(bias * normIndex);
            total += weights[i];
        }

        double roll = RANDOM.nextDouble() * total;
        double cumulative = 0.0;
        for (int i = 0; i < n; i++) {
            cumulative += weights[i];
            if (roll <= cumulative) {
                return (String) images.get(i).get("url");
            }
        }

        // Fallback: return newest (shouldn't be reached due to floating-point)
        return (String) images.get(n - 1).get("url");
    }

    private static double toDouble(Object value, double fallback) {
        if (value == null) return fallback;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString()); }
        catch (NumberFormatException e) { return fallback; }
    }
}