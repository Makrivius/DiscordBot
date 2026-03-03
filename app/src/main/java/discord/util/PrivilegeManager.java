package discord.util;

import discord.db.DatabaseManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PrivilegeManager {

    public enum Level {
        ADMIN, MODERATOR, NONE
    }

    // ─────────────────────────────────────────────
    // Access check
    // ─────────────────────────────────────────────

    /**
     * Resolves the highest privilege level a member has in their guild,
     * by checking their Discord roles against the privileges table.
     */
    public static Level getLevel(Member member) {
        String guildId = member.getGuild().getId();

        // Collect all Discord role IDs the member has
        Set<String> memberRoleIds = member.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        if (memberRoleIds.isEmpty()) return Level.NONE;

        // Fetch all mapped roles for this guild
        List<Map<String, Object>> rows = DatabaseManager.select(
                "privileges",
                new String[]{"guild_id"},
                new Object[]{guildId}
        );

        Level highest = Level.NONE;
        for (Map<String, Object> row : rows) {
            String roleId = (String) row.get("role_id");
            if (!memberRoleIds.contains(roleId)) continue;

            Level level = parseLevel((String) row.get("level"));
            if (level.ordinal() < highest.ordinal()) { // lower ordinal = higher privilege
                highest = level;
                if (highest == Level.ADMIN) break; // can't go higher
            }
        }
        return highest;
    }

    /**
     * Returns true if the member can edit an entry.
     * Allowed if: Discord guild owner, entry owner, or has admin/moderator role mapped in privileges.
     *
     * @param member        JDA Member object of the user trying to edit
     * @param entryOwnerId  owner_id stored on the DB entry
     * @param minLevel      minimum level required (e.g. Level.ADMIN or Level.MODERATOR)
     */
    public static boolean canEdit(Member member, String entryOwnerId, Level minLevel) {
        if (member.isOwner())                          return true;
        if (member.getId().equals(entryOwnerId))       return true;
        return getLevel(member).ordinal() <= minLevel.ordinal();
    }

    /** Shorthand — defaults to requiring at least MODERATOR access. */
    public static boolean canEdit(Member member, String entryOwnerId) {
        return canEdit(member, entryOwnerId, Level.MODERATOR);
    }

    // ─────────────────────────────────────────────
    // Privilege management (map Discord roles)
    // ─────────────────────────────────────────────

    /** Map a Discord role to an access level. Overwrites existing mapping. */
    public static void mapRole(String roleId, String guildId, Level level) {
        DatabaseManager.upsert(
                "privileges",
                new String[]{"role_id", "guild_id", "level"},
                new Object[]{roleId, guildId, level.name().toLowerCase()}
        );
    }

    /** Remove a Discord role mapping. */
    public static void unmapRole(String roleId, String guildId) {
        DatabaseManager.delete(
                "privileges",
                new String[]{"role_id", "guild_id"},
                new Object[]{roleId, guildId}
        );
    }

    /** List all role mappings for a guild. */
    public static List<Map<String, Object>> listMappings(String guildId) {
        return DatabaseManager.select(
                "privileges",
                new String[]{"guild_id"},
                new Object[]{guildId}
        );
    }

    // ─────────────────────────────────────────────
    // Internal
    // ─────────────────────────────────────────────

    private static Level parseLevel(String raw) {
        if (raw == null) return Level.NONE;
        return switch (raw.toLowerCase()) {
            case "admin"     -> Level.ADMIN;
            case "moderator" -> Level.MODERATOR;
            default          -> Level.NONE;
        };
    }
}