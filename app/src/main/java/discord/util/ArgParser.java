package discord.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgParser {
    private final Map<String, String> named = new HashMap<>();
    private final List<String> positional = new ArrayList<>();

    public ArgParser(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--")) {
                arg.toLowerCase();
                String body = arg.substring(2);
                int eq = body.indexOf('=');
                if (eq >= 0) {
                    named.put(body.substring(0, eq).toLowerCase(), body.substring(eq + 1));
                } else {
                    named.put(body, "true");
                }
            } else {
                positional.add(arg);
            }
        }
    }

    public String joinedQuery() {
        return String.join(" ", positional);
    }

    public List<String> positional() {
        return positional;
    }

    public Map<String, String> named() {
        return named;
    }
}
