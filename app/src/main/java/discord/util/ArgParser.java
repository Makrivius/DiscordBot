package discord.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArgParser {
    private final Map<String, String> named = new HashMap<>();
    private final Set<String> flags = new HashSet<>();
    private final List<String> positional = new ArrayList<>();

    public ArgParser(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String body = arg.substring(2);
                int eq = body.indexOf('=');
                if (eq >= 0) {
                    named.put(body.substring(0, eq).toLowerCase(), body.substring(eq + 1));
                } else {
                    flags.add(body.toLowerCase());
                }
            } else {
                positional.add(arg);
            }
        }
    }

    public boolean has(String flag) {
        return flags.contains(flag);
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
