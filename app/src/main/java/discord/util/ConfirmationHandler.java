package discord.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ConfirmationHandler extends ListenerAdapter {

    private static final ConcurrentHashMap<String, PendingAction> pending = new ConcurrentHashMap<>();

    /**
     * Permission check + confirmation flow in one call.
     * Denied with an embed if the member lacks access.
     */
    public static void requestWithPermission(MessageReceivedEvent event,
            String entryOwnerId,
            String description,
            Runnable onAccept) {
        Member member = event.getMember();
        if (member == null)
            return;

        if (!PrivilegeManager.canEdit(member, entryOwnerId)) {
            event.getChannel().sendMessageEmbeds(
                    new EmbedBuilder()
                            .setTitle("🚫 Permission denied")
                            .setDescription("Only the entry owner, a moderator, or an admin can edit this.")
                            .setColor(Color.RED)
                            .build())
                    .queue();
            return;
        }

        request(event, description, onAccept);
    }

    /** Plain confirmation with no permission check. */
    public static void request(MessageReceivedEvent event, String description, Runnable onAccept) {
        String userId = event.getAuthor().getId();

        // Delete previous pending message for this user if any
        PendingAction existing = pending.get(userId);
        if (existing != null && existing.message != null) {
            existing.message.delete().queue(null, ignored -> {
            });
        }

        event.getChannel().sendMessageEmbeds(
                new EmbedBuilder()
                        .setTitle("⚠️ Existing entry found")
                        .setDescription(description)
                        .setColor(Color.ORANGE)
                        .setFooter("This request will expire in 30 seconds")
                        .build())
                .setComponents(ActionRow.of(
                        Button.success("confirm_accept:" + userId, "✅ Accept"),
                        Button.danger("confirm_decline:" + userId, "❌ Decline")))
                .queue(sent -> {
                    pending.put(userId, new PendingAction(sent, onAccept));
                    sent.editMessageComponents()
                            .delay(30, TimeUnit.SECONDS)
                            .flatMap(m -> {
                                pending.remove(userId);
                                return m.editMessageEmbeds(
                                        new EmbedBuilder()
                                                .setTitle("⏱️ Request expired")
                                                .setColor(Color.GRAY)
                                                .build())
                                        .setComponents();
                            })
                            .queue(null, ignored -> {
                            });
                });
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (!componentId.startsWith("confirm_accept:") && !componentId.startsWith("confirm_decline:"))
            return;

        String userId = componentId.split(":")[1];

        if (!event.getUser().getId().equals(userId)) {
            event.reply("❌ This confirmation isn't for you.").setEphemeral(true).queue();
            return;
        }

        PendingAction action = pending.remove(userId);
        if (action == null) {
            event.reply("⏱️ This request has already expired.").setEphemeral(true).queue();
            return;
        }

        if (componentId.startsWith("confirm_accept:")) {
            action.onAccept.run();
            event.editMessageEmbeds(
                    new EmbedBuilder()
                            .setTitle("✅ Changes accepted")
                            .setDescription("The entry has been updated successfully.")
                            .setColor(Color.GREEN)
                            .build())
                    .setComponents().queue();
        } else {
            event.editMessageEmbeds(
                    new EmbedBuilder()
                            .setTitle("❌ Changes declined")
                            .setDescription("No changes were made.")
                            .setColor(Color.RED)
                            .build())
                    .setComponents().queue();
        }
    }

    private static class PendingAction {
        final Message message;
        final Runnable onAccept;

        PendingAction(Message message, Runnable onAccept) {
            this.message = message;
            this.onAccept = onAccept;
        }
    }
}