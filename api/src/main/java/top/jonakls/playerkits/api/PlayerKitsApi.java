package top.jonakls.playerkits.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pk.ajneb97.api.model.kit.KitModel;
import pk.ajneb97.api.model.player.PlayerModel;
import pk.ajneb97.api.utils.PlayerUtils;
import top.jonakls.playerkits.api.manager.DataManager;

/**
 * Public API for PlayerKits
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerKitsApi {

    private final DataManager<PlayerModel> playerModelDataManager;
    private final DataManager<KitModel> kitDataManager;

    /**
     * Constructor for PlayerKitsApi
     *
     * @param playerModelDataManager The DataManager for PlayerModel
     * @param kitDataManager         The DataManager for KitModel
     */
    public PlayerKitsApi(
            final @NotNull DataManager<PlayerModel> playerModelDataManager,
            final @NotNull DataManager<KitModel> kitDataManager
    ) {
        this.playerModelDataManager = playerModelDataManager;
        this.kitDataManager = kitDataManager;
    }


    /**
     * Get the cooldown of a kit for a player in milliseconds
     *
     * @param player  The player to get the cooldown for
     * @param kitName The name of the kit to get the cooldown for
     * @return The cooldown of the kit for the player in milliseconds
     */
    public long getKitCooldown(final @NotNull Player player, final @NotNull String kitName) {
        final KitModel kitModel = kitDataManager.get(kitName);
        final PlayerModel playerModel = playerModelDataManager.get(player.getUniqueId().toString());

        final long playerCooldown = playerModel.getKitCooldown(kitName);
        if (kitModel.getCooldown() != 0 && !PlayerUtils.isPlayerKitsAdmin(player)) {
            final long cooldown = playerCooldown - System.currentTimeMillis();
            if (cooldown > 0) {
                return cooldown;
            }
        }
        return 0;
    }


    /**
     * Get if a kit is one time ready for a player
     *
     * @param player  The player to get the one time ready status for
     * @param kitName The name of the kit to get the one time ready status for
     * @return "yes" if the kit is one time ready, "no" if the kit is not one time ready
     */
    public boolean getOneTimeReady(final @NotNull Player player, final @NotNull String kitName) {
        final PlayerModel playerModel = playerModelDataManager.get(player.getUniqueId().toString());
        return playerModel.getKitOneTime(kitName);
    }
}
