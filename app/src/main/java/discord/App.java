package discord;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord.AudioPlayer.AudioPlayerManagerHolder;
import discord.Guild.SessionRegistry;
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
                SessionRegistry session = new SessionRegistry(audioHolder.get());

                JDA jda = JDABuilder
                                .createDefault(config.token())
                                .enableIntents(EnumSet
                                                .of(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT))
                                .build().awaitReady();
                log.info("Bot is ready as {}", jda.getSelfUser().getAsTag());
        }
}