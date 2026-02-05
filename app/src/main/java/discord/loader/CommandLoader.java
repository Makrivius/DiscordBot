package discord.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import discord.commands.BaseCommand;

public class CommandLoader {

    public static Map<String, BaseCommand> load(String basePackage) {
        Map<String, BaseCommand> commands = new HashMap<>();

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage(basePackage)
                .addUrls(ClasspathHelper.forPackage(basePackage)) // only your compiled classes
                .setScanners(Scanners.SubTypes));
        Set<Class<? extends BaseCommand>> classes = reflections.getSubTypesOf(BaseCommand.class);

        for (Class<? extends BaseCommand> clazz : classes) {
            try {
                BaseCommand cmd = clazz.getDeclaredConstructor().newInstance();
                commands.put(cmd.getName(), cmd);
            } catch (Exception e) {
                System.err.println("Failed to load command: " + clazz.getName());
            }
        }

        return commands;
    }
}
