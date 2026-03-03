package discord.loader;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import discord.commands.BaseCommand;
import discord.commands.Help;

public class CommandLoader {

    public static Map<String, BaseCommand> load(String basePackage) {
        Map<String, BaseCommand> commands = new LinkedHashMap<>();

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage(basePackage)
                .addUrls(ClasspathHelper.forPackage(basePackage))
                .setScanners(Scanners.SubTypes));

        Set<Class<? extends BaseCommand>> classes = reflections.getSubTypesOf(BaseCommand.class);

        for (Class<? extends BaseCommand> clazz : classes) {
            // Help is registered separately after all commands are loaded
            // since it needs the full command map in its constructor
            if (clazz.equals(Help.class)) continue;

            try {
                BaseCommand cmd = clazz.getDeclaredConstructor().newInstance();
                commands.put(cmd.getName(), cmd);
            } catch (Exception e) {
                System.err.println("Failed to load command: " + clazz.getName() + " — " + e.getMessage());
            }
        }

        // Register Help last with full command list
        commands.put("help", new Help(commands.values()));

        return commands;
    }
}