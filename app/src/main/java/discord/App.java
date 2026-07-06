package discord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class App {
        public static void main(String[] args) throws Exception {
                BotConfig.ensureLoaded();
                final Logger log = LoggerFactory.getLogger(App.class);
                JDA jda = JDABuilder.createDefault(BotConfig.TOKEN).build().awaitReady();
        }
}