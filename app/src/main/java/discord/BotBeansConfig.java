package discord;

import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;

import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import discord.audioPlayer.players.lavalink.nodes.NodeHealthChecker;
import discord.commands.CommandRegistry;
import discord.commands.DiscordCommandDispatcher;
import discord.guild.SessionRegistry;
import discord.hooks.DiscordListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Configuration
public class BotBeansConfig {
    private static final Logger log = LoggerFactory.getLogger(BotBeansConfig.class);

    @Bean
    LavalinkClient lavalinkClient(BotProperties properties) {
        long userId = Helpers.getUserIdFromToken(properties.token());
        LavalinkClient lavalinkClient = new LavalinkClient(userId);
        new NodeHealthChecker().registerAndWatch(lavalinkClient);
        return lavalinkClient;
    }

    @Bean
    ScheduledExecutorService retryExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Bean
    DiscordCommandDispatcher discordCommandDispatcher(CommandRegistry registry, SessionRegistry sessions,
            BotProperties properties) {
        return new DiscordCommandDispatcher(registry, sessions, properties.defaultPrefix());
    }

    @Bean
    AudioPlayerManager audioPlayerManager() {
        var manager = new DefaultAudioPlayerManager();
        manager.registerSourceManager(new YoutubeAudioSourceManager());
        return manager;
    }

    @Bean
    JDA jda(BotProperties properties, LavalinkClient lavalinkClient, DiscordCommandDispatcher dispatcher)
            throws InterruptedException {
        EnumSet<GatewayIntent> gatewayIntents = EnumSet.of(GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.MESSAGE_CONTENT);
        if (gatewayIntents == null) {
            log.error("None gateway intents are persisted!");
            throw new InterruptedException();
        }
        JDA jda = JDABuilder.createDefault(properties.token()).enableIntents(gatewayIntents)
                .addEventListeners(new DiscordListener(dispatcher))
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalinkClient))
                .setAudioModuleConfig(new AudioModuleConfig().withDaveSessionFactory(new JDaveSessionFactory())).build()
                .awaitReady();
        log.info("Bot is ready as {}", jda.getSelfUser().getAsTag());
        return jda;
    }
}
