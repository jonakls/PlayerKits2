package pk.ajneb97.api;

import org.bukkit.entity.Player;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.api.model.kit.KitModel;
import pk.ajneb97.api.utils.PlayerUtils;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.managers.PlayerDataManager;

@Deprecated(since = "1.0.0", forRemoval = true)
public class PlayerKitsAPI {

    private static PlayerKits2 plugin;
    public PlayerKitsAPI(PlayerKits2 plugin){
        this.plugin = plugin;
    }

    public static String getKitCooldown(Player player, String kitName){
        KitModel kitModel = plugin.getKitsManager().getKitByName(kitName);
        MessagesManager messagesManager = plugin.getMessagesManager();

        if(kitModel == null){
            return null;
        }

        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        long playerCooldown = playerDataManager.getKitCooldown(player, kitModel.getName());
        if(kitModel.getCooldown() != 0 && !PlayerUtils.isPlayerKitsAdmin(player)){
            String timeStringMillisDif = playerDataManager.getKitCooldownString(playerCooldown);
            if(!timeStringMillisDif.isEmpty()) {
                return timeStringMillisDif;
            }
        }

        return messagesManager.getCooldownPlaceholderReady();
    }

    public static String getOneTimeReady(Player player, String kitName){
        KitModel kitModel = plugin.getKitsManager().getKitByName(kitName);
        if(kitModel == null){
            return null;
        }

        boolean oneTime = plugin.getPlayerDataManager().isKitOneTime(player,kitName);
        if(oneTime){
            return "yes";
        }else{
            return "no";
        }
    }
}
