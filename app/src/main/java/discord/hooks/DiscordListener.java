package discord.hooks;

import javax.annotation.Nonnull;

import discord.commands.CommandRegistry;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordListener extends ListenerAdapter {
    private final CommandRegistry registry;

    public DiscordListener(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;
        registry.handle(event);
    }
}
