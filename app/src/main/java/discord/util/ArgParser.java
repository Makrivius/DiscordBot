package discord.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArgParser {
    private final Set<String> flags = new HashSet<>();
    private final List<String> positional = new ArrayList<>();

    public ArgParser(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--")) {
                flags.add(arg.substring(2).toLowerCase());
            } else {
                positional.add(arg);
            }
        }
    }

    public boolean has(String flag) {
        return flags.contains(flag);
    }

    public String joinedQuery() {
        return String.join("", positional);
    }

    public List<String> positional() {
        return positional;
    }
}
