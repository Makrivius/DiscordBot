package discord.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;

public class JsonUtil {
    public static Map<String, String> toStringMap(JsonObject json, String... exclude) {
        Map<String, String> named = new HashMap<>();
        Set<String> excludedKeys = Set.of(exclude);

        json.entrySet().forEach(e -> {
            if (!excludedKeys.contains(e.getKey())) {
                named.put(e.getKey(), e.getValue().getAsString());
            }
        });
        return named;
    }
}
