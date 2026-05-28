package discord.enums;

public enum PlayerAction {
    PAUSE("player_pause"),
    PREVIOUS("player_previous"),
    SKIP("player_skip"),
    STOP("player_stop"),
    LOOP("player_loop");

    public final String id;

    PlayerAction(String id) {
        this.id = id;
    }

    public static PlayerAction fromId(String id) {
        for (var a : values()) {
            if (a.id.equals(id))
                return a;
        }
        return null;
    }
}
