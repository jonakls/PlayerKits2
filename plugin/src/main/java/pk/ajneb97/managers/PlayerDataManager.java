package pk.ajneb97.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.database.MySQLConnection;
import pk.ajneb97.api.model.player.PlayerModel;
import pk.ajneb97.api.model.player.PlayerKitsMessageResult;
import pk.ajneb97.utils.OtherUtils;

import java.util.ArrayList;

public class PlayerDataManager {

    private PlayerKits2 plugin;
    private ArrayList<PlayerModel> players;

    public PlayerDataManager(PlayerKits2 plugin){
        this.plugin = plugin;
    }

    public ArrayList<PlayerModel> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<PlayerModel> players) {
        this.players = players;
    }

    private PlayerModel getPlayer(Player player, boolean create) {
        for (PlayerModel playerModel : players) {
            if (playerModel.id().equals(player.getUniqueId().toString())) {
                return playerModel;
            }
        }

        if(create){
            PlayerModel playerModel = new PlayerModel(player.getName(), player.getUniqueId().toString());
            playerModel.setModified(true);
            players.add(playerModel);
            return playerModel;
        }
        return null;
    }

    public PlayerModel getPlayerByUUID(String uuid) {
        for (PlayerModel player : players) {
            if (player.id().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    public void removePlayerByUUID(String uuid){
        for(int i=0;i<players.size();i++){
            if (players.get(i).id().equals(uuid)) {
                players.remove(i);
                return;
            }
        }
    }

    public PlayerModel getPlayerByName(String name) {
        for (PlayerModel player : players) {
            if(player.getName().equals(name)){
                return player;
            }
        }
        return null;
    }

    public void setKitCooldown(Player player,String kitName,long cooldown){
        PlayerModel playerModel = getPlayer(player, true);
        boolean creating = playerModel.setKitCooldown(kitName, cooldown);
        playerModel.setModified(true);
        if(plugin.getMySQLConnection() != null){
            plugin.getMySQLConnection().updateKit(playerModel, playerModel.getKit(kitName), creating);
        }
    }

    public long getKitCooldown(Player player,String kitName){
        PlayerModel playerModel = getPlayerByUUID(player.getUniqueId().toString());
        if (playerModel == null) {
            return 0;
        }else{
            return playerModel.getKitCooldown(kitName);
        }
    }

    public String getKitCooldownString(long playerCooldown){
        long currentMillis = System.currentTimeMillis();
        long millisDif = playerCooldown-currentMillis;
        String timeStringMillisDif = OtherUtils.getTime(millisDif/1000, plugin.getMessagesManager());
        return timeStringMillisDif;
    }

    public void setKitOneTime(Player player,String kitName){
        PlayerModel playerModel = getPlayer(player, true);
        boolean creating = playerModel.setKitOneTime(kitName);
        playerModel.setModified(true);
        if(plugin.getMySQLConnection() != null){
            plugin.getMySQLConnection().updateKit(playerModel, playerModel.getKit(kitName), creating);
        }
    }

    public boolean isKitOneTime(Player player,String kitName){
        PlayerModel playerModel = getPlayerByUUID(player.getUniqueId().toString());
        if (playerModel == null) {
            return false;
        }else{
            return playerModel.getKitOneTime(kitName);
        }
    }

    public void setKitBought(Player player,String kitName){
        PlayerModel playerModel = getPlayer(player, true);
        boolean creating = playerModel.setKitBought(kitName);
        playerModel.setModified(true);
        if(plugin.getMySQLConnection() != null){
            plugin.getMySQLConnection().updateKit(playerModel, playerModel.getKit(kitName), creating);
        }
    }

    public boolean isKitBought(Player player,String kitName){
        PlayerModel playerModel = getPlayerByUUID(player.getUniqueId().toString());
        if (playerModel == null) {
            return false;
        }else{
            return playerModel.getKitHasBought(kitName);
        }
    }

    public PlayerKitsMessageResult resetKitForPlayer(String name, String kitName){
        PlayerModel playerModel = getPlayerByName(name);
        FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        if (playerModel == null) {
            return PlayerKitsMessageResult.error(messagesConfig.getString("playerDataNotFound")
                    .replace("%player%",name));
        }

        playerModel.resetKit(kitName);
        playerModel.setModified(true);
        if(plugin.getMySQLConnection() != null){
            plugin.getMySQLConnection().resetKit(playerModel.id(), kitName);
        }

        return PlayerKitsMessageResult.success();
    }

    public void manageJoin(Player player){
        // Create or update data
        if(plugin.getMySQLConnection() != null){
            MySQLConnection mySQLConnection = plugin.getMySQLConnection();
            String uuid = player.getUniqueId().toString();
            mySQLConnection.getPlayer(uuid, playerData -> {
                removePlayerByUUID(uuid); //Remove data if already exists
                boolean firstJoin = false;
                if(playerData != null) {
                    players.add(playerData);
                    //Update name if different
                    if(!playerData.getName().equals(player.getName())){
                        playerData.setName(player.getName());
                        mySQLConnection.updatePlayerName(playerData);
                    }
                }else {
                    firstJoin = true;
                    playerData = new PlayerModel(player.getName(), uuid);
                    players.add(playerData);
                    //Create if it doesn't exist
                    mySQLConnection.createPlayer(playerData);
                }
                if(firstJoin){
                    plugin.getKitsManager().giveFirstJoinKit(player);
                }
            });
        }else{
            boolean firstJoin = false;
            PlayerModel playerModel = getPlayerByUUID(player.getUniqueId().toString());
            if (playerModel == null) {
                firstJoin = true;
                playerModel = new PlayerModel(player.getName(), player.getUniqueId().toString());
                playerModel.setModified(true);
                players.add(playerModel);
            }else{
                if (!playerModel.getName().equals(player.getName())) {
                    playerModel.setName(player.getName());
                    playerModel.setModified(true);
                }
            }

            if(firstJoin){
                plugin.getKitsManager().giveFirstJoinKit(player);
            }
        }
    }
}
