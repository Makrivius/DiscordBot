package discord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import net.dv8tion.jda.api.JDA;

@SpringBootApplication
@EnableConfigurationProperties(BotProperties.class)
public class App {
        private static final Logger log = LoggerFactory.getLogger(App.class);

        public static void main(String[] args) {
                SpringApplication.run(App.class, args);
        }

        @Bean
        CommandLineRunner onJDAReady(JDA jda) {
                return args -> {
                        log.info("JDA is ready!");
                        log.info("Logged in as: " + jda.getSelfUser().getAsTag());
                };
        }
}