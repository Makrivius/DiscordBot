package discord.commands;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandRegistry {
    private static final Logger log = LoggerFactory.getLogger(CommandRegistry.class);
    private final Map<String, Command> commands = new HashMap<>();
    private final String prefix;
    private final Map<Class<?>, Object> dependencies = new HashMap<>();

    public CommandRegistry(String prefix) {
        this.prefix = prefix;
    }

    public void provide(Object dependency) {
        dependencies.put(dependency.getClass(), dependency);
    }

    public void discoverAndRegister(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<? extends Command>> found = reflections.getSubTypesOf(Command.class);

        for (Class<? extends Command> cls : found) {
            try {
                Command instance = instantiate(cls);
                commands.put(instance.name().toLowerCase(), instance);
                log.info("Registered command: {}{}", prefix, instance.name());
            } catch (Exception e) {
                log.error("Failed to load command class {}", cls.getName(), e);
            }
        }
        log.info("Loaded {} commands total", commands.size());
    }

    public Command get(String name) {
        return commands.get(name.toLowerCase());
    }

    private Command instantiate(Class<? extends Command> cls) throws Exception {
        Constructor<?> constructor = cls.getDeclaredConstructors()[0];
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            Object dep = dependencies.get(paramTypes[i]);
            if (dep == null) {
                throw new IllegalStateException(
                        "No dependency registered for " + paramTypes[i].getSimpleName() + " needed by "
                                + cls.getSimpleName());
            }
            args[i] = dep;
        }
        return (Command) constructor.newInstance(args);
    }

}
