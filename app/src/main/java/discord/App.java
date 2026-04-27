package discord;

import java.time.ZoneId;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import discord.commands.BaseCommand;
import discord.commands.CommandRegistry;
import discord.db.Schema;
import discord.loader.CommandLoader;
import discord.scheduler.EventScheduler;
import discord.handlers.ConfirmationHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class App {
        public static void main(String[] args) throws Exception {
                Config.ensureLoaded();
                final Logger log = LoggerFactory.getLogger(App.class);

                Schema.init();

                Map<String, BaseCommand> commands = CommandLoader.load("discord.commands");

                JDA api = JDABuilder
                                .createDefault(Config.TOKEN,
                                                GatewayIntent.GUILD_MESSAGES,
                                                GatewayIntent.MESSAGE_CONTENT,
                                                GatewayIntent.GUILD_VOICE_STATES)
                                .addEventListeners(
                                                new CommandRegistry(commands),
                                                new ConfirmationHandler())
                                .setAudioModuleConfig(
                                                new AudioModuleConfig()
                                                                .withDaveSessionFactory(new JDaveSessionFactory()))
                                .build();

                api.awaitReady();

                // Start scheduler — adjust timezone to your server's location
                EventScheduler scheduler = new EventScheduler(api, ZoneId.of("UTC"));
                scheduler.start();

                log.info("Bot is online with {} commands loaded", commands.size());

                // Graceful shutdown
                Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));
        }
}