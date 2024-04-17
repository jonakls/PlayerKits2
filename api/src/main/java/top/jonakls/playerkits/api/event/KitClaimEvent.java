package top.jonakls.playerkits.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class KitClaimEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String kitName;
    private final Player player;

    public KitClaimEvent(final @NotNull Player player, final @NotNull String kitName) {
        this.player = player;
        this.kitName = kitName;
    }

    public String getKitName() {
        return kitName;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
