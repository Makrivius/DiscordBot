package discord;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord.audioPlayer.AudioPlayerManagerHolder;
import discord.commands.CommandRegistry;
import discord.guild.SessionRegistry;
import discord.hooks.DiscordListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class App {
        private static final Logger log = LoggerFactory.getLogger(App.class);

        public static void main(String[] args) throws Exception {
                Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
                        log.error("Uncaught exception on thread {}", thread.getName(), ex);
                });

                BotConfig config = BotConfig.load();
                log.info("Config loaded, WS port: {}", config.wsPort());

                AudioPlayerManagerHolder audioHolder = new AudioPlayerManagerHolder();
                SessionRegistry sessions = new SessionRegistry(audioHolder.get());

                CommandRegistry registry = new CommandRegistry(config.defaultPrefix());
                registry.provide(sessions);
                registry.discoverAndRegister("discord.commands");

                JDA jda = JDABuilder
                                .createDefault(config.token())
                                .enableIntents(EnumSet
                                                .of(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT))
                                .addEventListeners(new DiscordListener(registry))
                                .build().awaitReady();
                log.info("Bot is ready as {}", jda.getSelfUser().getAsTag());
        }
}