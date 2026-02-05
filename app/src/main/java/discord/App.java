package discord;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord.commands.BaseCommand;
import discord.handlers.CommandListener;
import discord.loader.CommandLoader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class App {
    public static void main(String[] args) throws Exception {
        final Logger log = LoggerFactory.getLogger(App.class);

        // 1. Load all commands from your package
        Map<String, BaseCommand> commands = CommandLoader.load("discord.commands");

        // 2. Build JDA and register your listener
        JDA api = JDABuilder
                .createDefault(Config.TOKEN,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_VOICE_STATES)
                .addEventListeners(new CommandListener(commands))
                .build();

        // 3. Wait for JDA to be ready (optional but recommended)
        api.awaitReady();

        log.info("Bot is online with {} commands", commands.size());
    }
}
