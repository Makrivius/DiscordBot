package discord.commands;

import discord.Config;
import discord.util.CommandParser.ParsedCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.Collection;

public class Help extends BaseCommand {

    private final Collection<BaseCommand> commands;

    /** Pass in all registered commands so Help can introspect them. */
    public Help(Collection<BaseCommand> commands) {
        this.commands = commands;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Shows all commands or details about a specific one.";
    }

    @Override
    public String getUsage() {
        return """
                `%shelp` — list all commands
                `%shelp <command>` — show details for a command
                """.formatted(Config.PREFIX, Config.PREFIX);
    }

    @Override
    public void execute(MessageReceivedEvent event, ParsedCommand cmd) {
        if (cmd.args.isEmpty()) {
            showAll(event);
        } else {
            showOne(event, cmd.args.get(0).toLowerCase());
        }
    }

    // ─────────────────────────────────────────────
    // !help → list all
    // ─────────────────────────────────────────────

    private void showAll(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📖 Commands")
                .setColor(Color.CYAN)
                .setFooter("Use " + Config.PREFIX + "help <command> for details");

        for (BaseCommand cmd : commands) {
            embed.addField(
                    Config.PREFIX + cmd.getName(),
                    cmd.getDescription(),
                    false);
        }

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    // ─────────────────────────────────────────────
    // !help <command> → single command detail
    // ─────────────────────────────────────────────

    private void showOne(MessageReceivedEvent event, String name) {
        BaseCommand found = commands.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (found == null) {
            event.getChannel().sendMessage(
                    "❌ Unknown command `" + name + "`. Use `" + Config.PREFIX + "help` to see all commands.").queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Config.PREFIX + found.getName())
                .setDescription(found.getDescription())
                .addField("Usage", found.getUsage(), false)
                .setColor(Color.CYAN);

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}