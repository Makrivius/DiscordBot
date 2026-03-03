package discord.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import discord.Config;

public class CommandParser {

    public static ParsedCommand parse(String raw) {
        raw = raw.trim();
        String prefix = Config.PREFIX;

        if (!raw.startsWith(prefix)) {
            return null;
        }

        // Remove prefix
        raw = raw.substring(prefix.length()).trim();

        // Extract command name
        int spaceIndex = raw.indexOf(" ");
        String command;
        String argsRaw;

        if (spaceIndex == -1) {
            command = raw.toLowerCase();
            argsRaw = "";
        } else {
            command = raw.substring(0, spaceIndex).toLowerCase();
            argsRaw = raw.substring(spaceIndex + 1).trim();
        }

        // Split args by spaces but keep quoted strings intact
        List<String> args = new ArrayList<>();
        Matcher m = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(argsRaw);
        while (m.find()) {
            if (m.group(1) != null)
                args.add(m.group(1));
            else
                args.add(m.group(2));
        }

        return new ParsedCommand(command, args, argsRaw);
    }

    public static class ParsedCommand {
        public final String command;
        public final List<String> args;
        public final String rawArgs;

        public ParsedCommand(String command, List<String> args, String rawArgs) {
            this.command = command;
            this.args = args;
            this.rawArgs = rawArgs;
        }
    }
}
