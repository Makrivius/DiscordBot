package discord.commands;

public interface Command {
    String name();

    void execute(CommandContext ctx);
}
