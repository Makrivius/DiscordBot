package discord.hooks;

import discord.commands.DiscordCommandDispatcher;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordListener extends ListenerAdapter {
    private final DiscordCommandDispatcher dispatcher;

    public DiscordListener(DiscordCommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void onMessageReceived(@SuppressWarnings("null") MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;
        dispatcher.handle(event);
    }
}
