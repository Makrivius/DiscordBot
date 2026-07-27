package discord;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import discord.audioPlayer.AudioPlayerManagerHolder;
import discord.commands.CommandRegistry;
import discord.commands.DiscordCommandDispatcher;
import discord.guild.SessionRegistry;
import discord.hooks.DiscordListener;
import discord.http.AppServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class App {
        private static final Logger log = LoggerFactory.getLogger(App.class);

        public static void main(String[] args) throws Exception {
                Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
                        log.error("Uncaught exception on thread {}", thread.getName(), ex);
                });

                BotConfig config = BotConfig.load();
                log.info("Config loaded ");

                AudioPlayerManagerHolder audioHolder = new AudioPlayerManagerHolder();
                SessionRegistry sessions = new SessionRegistry(audioHolder);

                CommandRegistry registry = new CommandRegistry(config.defaultPrefix());
                registry.provide(sessions);
                registry.discoverAndRegister("discord.commands");

                DiscordCommandDispatcher dispatcher = new DiscordCommandDispatcher(registry, sessions,
                                config.defaultPrefix());

                EnumSet<GatewayIntent> gatewayIntents = EnumSet
                                .of(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT);
                if (gatewayIntents.isEmpty()) {
                        log.error("Gateway intents are missing!");
                        return;
                }
                JDA jda = JDABuilder
                                .createDefault(config.token())
                                .enableIntents(gatewayIntents)
                                .addEventListeners(new DiscordListener(dispatcher))
                                .setAudioModuleConfig(new AudioModuleConfig()
                                                .withDaveSessionFactory(new JDaveSessionFactory()))
                                .build().awaitReady();
                log.info("Bot is ready as {}", jda.getSelfUser().getAsTag());

                AppServer server = new AppServer(config.clientId(), config.secret(), sessions, registry, jda);
                server.start(config.wsPort());
                log.info("Server started on port {}", config.wsPort());
        }
}