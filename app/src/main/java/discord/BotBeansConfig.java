package discord;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import discord.audioPlayer.AudioPlayerManagerHolder;
import discord.audioPlayer.nodes.NodeHealthChecker;
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
    AudioPlayerManagerHolder audioPlayerManagerHolder(BotProperties properties) {
        return new AudioPlayerManagerHolder(properties.token());
    }

    @Bean
    SessionRegistry sessionRegistry(AudioPlayerManagerHolder audioHolder) {
        return new SessionRegistry(audioHolder);
    }

    @Bean
    LavalinkClient lavalinkClient(AudioPlayerManagerHolder audioHolder) {
        LavalinkClient client = audioHolder.getAudioPlayerManager();
        new NodeHealthChecker().registerAndWatch(client);
        return client;
    }

    @Bean
    DiscordCommandDispatcher discordCommandDispatcher(CommandRegistry registry, SessionRegistry sessions,
            BotProperties properties) {
        return new DiscordCommandDispatcher(registry, sessions, properties.defaultPrefix());
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
        JDA jda = JDABuilder.createDefault(properties.token())
                .enableIntents(gatewayIntents)
                .addEventListeners(new DiscordListener(dispatcher))
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalinkClient))
                .setAudioModuleConfig(new AudioModuleConfig().withDaveSessionFactory(new JDaveSessionFactory()))
                .build().awaitReady();
        log.info("Bot is ready as {}", jda.getSelfUser().getAsTag());
        return jda;
    }
}
