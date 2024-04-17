package pk.ajneb97.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.configs.MainConfigManager;
import pk.ajneb97.api.model.kit.KitModel;
import pk.ajneb97.api.model.kit.KitAction;
import pk.ajneb97.api.model.kit.GiveKitInstructions;
import pk.ajneb97.api.model.gui.KitPosition;
import pk.ajneb97.api.model.gui.KitVariable;
import pk.ajneb97.api.model.player.PlayerKitsMessageResult;
import pk.ajneb97.api.model.gui.InventoryPlayer;
import pk.ajneb97.api.model.gui.ItemKitInventory;
import pk.ajneb97.api.model.gui.KitInventory;
import pk.ajneb97.api.model.kit.item.KitItem;
import pk.ajneb97.utils.ItemUtils;
import pk.ajneb97.api.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

public class InventoryManager {

    private PlayerKits2 plugin;
    private ArrayList<KitInventory> inventories;
    private ArrayList<InventoryPlayer> players;
    private InventoryRequirementsManager inventoryRequirementsManager;
    public InventoryManager(PlayerKits2 plugin){
        this.plugin = plugin;
        this.players = new ArrayList<>();
        this.inventoryRequirementsManager = new InventoryRequirementsManager(plugin,this);
    }

    public ArrayList<InventoryPlayer> getPlayers() {
        return players;
    }

    public ArrayList<KitInventory> getInventories() {
        return inventories;
    }

    public InventoryRequirementsManager getInventoryRequirementsManager() {
        return inventoryRequirementsManager;
    }

    public void setInventories(ArrayList<KitInventory> inventories) {
        this.inventories = inventories;
    }

    public KitInventory getInventory(String name){
        for(KitInventory kitInventory : inventories){
            if(kitInventory.getName().equals(name)){
                return kitInventory;
            }
        }
        return null;
    }

    public InventoryPlayer getInventoryPlayer(Player player){
        for(InventoryPlayer inventoryPlayer : players){
            if(inventoryPlayer.getPlayer().equals(player)){
                return inventoryPlayer;
            }
        }
        return null;
    }

    public void removeInventoryPlayer(Player player){
        for(int i=0;i<players.size();i++){
            if(players.get(i).getPlayer().equals(player)){
                players.remove(i);
            }
        }
    }

    public void openInventory(InventoryPlayer inventoryPlayer){
        KitInventory kitInventory = getInventory(inventoryPlayer.getInventoryName());

        String title = kitInventory.getTitle();
        if(inventoryPlayer.getInventoryName().equals("buy_requirements_inventory") || inventoryPlayer.getInventoryName().equals("preview_inventory")){
            title = title.replace("%kit%",inventoryPlayer.getKitName());
        }
        Inventory inv = Bukkit.createInventory(null,kitInventory.getSlots(),
                MessagesManager.getColoredMessage(title));

        List<ItemKitInventory> items = kitInventory.getItems();
        KitItemManager kitItemManager = plugin.getKitItemManager();
        KitsManager kitsManager = plugin.getKitsManager();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        MessagesManager msgManager = plugin.getMessagesManager();

        //Add items for all inventories
        for(ItemKitInventory itemInventory : items){
            for(int slot : itemInventory.getSlots()){
                String type = itemInventory.getType();
                if(type != null){
                    if(type.startsWith("kit: ")){
                        setKit(type.replace("kit: ",""),inventoryPlayer.getPlayer(),inv,slot,kitsManager
                                ,playerDataManager,kitItemManager,msgManager);
                    }
                    continue;
                }

                ItemStack item = kitItemManager.createItemFromKitItem(itemInventory.getItem(),inventoryPlayer.getPlayer());

                if(inventoryPlayer.getInventoryName().equals("buy_requirements_inventory")){
                    inventoryRequirementsManager.configureRequirementsItem(item,inventoryPlayer.getKitName(),inventoryPlayer.getPlayer());
                }

                String openInventory = itemInventory.getOpenInventory();
                if(openInventory != null) {
                    item = ItemUtils.setTagStringItem(plugin,item, "playerkits_open_inventory", openInventory);
                }
                List<String> commands = itemInventory.getCommands();
                if(commands != null && !commands.isEmpty()) {
                    String commandList = "";
                    for(int i=0;i<commands.size();i++) {
                        if(i==commands.size()-1) {
                            commandList=commandList+commands.get(i);
                        }else {
                            commandList=commandList+commands.get(i)+"|";
                        }
                    }
                    item = ItemUtils.setTagStringItem(plugin,item, "playerkits_item_commands", commandList);
                }

                inv.setItem(slot,item);
            }
        }

        //Special items for some inventories
        if(inventoryPlayer.getInventoryName().equals("preview_inventory")){
            setKitPreviewItems(inv,inventoryPlayer,kitInventory);
        }


        inventoryPlayer.getPlayer().openInventory(inv);
        players.add(inventoryPlayer);
    }

    public void setKitPreviewItems(Inventory inv,InventoryPlayer inventoryPlayer,KitInventory kitInventory){
        KitItemManager kitItemManager = plugin.getKitItemManager();
        KitsManager kitsManager = plugin.getKitsManager();

        KitModel kitModel = kitsManager.getKitByName(inventoryPlayer.getKitName());
        if(kitModel == null){
            return;
        }

        // Create a list with all items including actions display items
        ArrayList<KitItem> allItems = new ArrayList<>();
        allItems.addAll(kitModel.getItems());
        for(KitAction kitAction : kitModel.getClaimActions()){
            KitItem kitItem = kitAction.getDisplayItem();
            if(kitItem != null){
                allItems.add(kitItem);
            }
        }

        int slot = 0;
        for(KitItem kitItem : allItems){
            ItemStack item = kitItemManager.createItemFromKitItem(kitItem,inventoryPlayer.getPlayer());
            if(kitItem.getPreviewSlot() != -1){
                inv.setItem(kitItem.getPreviewSlot(),item);
            }else{
                inv.setItem(slot,item);
            }
            slot++;

            if(slot >= kitInventory.getSlots()){
                break;
            }
        }
    }

    public void clickInventory(InventoryPlayer inventoryPlayer, ItemStack item, ClickType clickType){
        String kitName = ItemUtils.getTagStringItem(plugin,item,"playerkits_kit");
        if(kitName != null){
            clickOnKitItem(inventoryPlayer,kitName,clickType);
            return;
        }

        String openInventory = ItemUtils.getTagStringItem(plugin,item,"playerkits_open_inventory");
        if(openInventory != null){
            clickOnOpenInventoryItem(inventoryPlayer,openInventory);
            return;
        }

        String itemCommands = ItemUtils.getTagStringItem(plugin,item,"playerkits_item_commands");
        if(itemCommands != null){
            clickOnCommandItem(inventoryPlayer,itemCommands);
        }

        //Requirements inventory
        if(inventoryPlayer.getInventoryName().equals("buy_requirements_inventory")){
            String buyTag = ItemUtils.getTagStringItem(plugin,item,"playerkits_buy");
            if(buyTag == null){
                return;
            }
            if(buyTag.equals("yes")){
                inventoryRequirementsManager.requirementsInventoryBuy(inventoryPlayer);
            }else{
                inventoryRequirementsManager.requirementsInventoryCancel(inventoryPlayer);
            }
        }
    }

    public void clickOnKitItem(InventoryPlayer inventoryPlayer,String kitName,ClickType clickType){
        MainConfigManager mainConfigManager = plugin.getConfigsManager().getMainConfigManager();
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MessagesManager msgManager = plugin.getMessagesManager();

        if(clickType.equals(ClickType.RIGHT)){
            //Preview
            if(!mainConfigManager.isKitPreview()){
                return;
            }

            KitModel kitModel = plugin.getKitsManager().getKitByName(kitName);
            if(kitModel.isPermissionRequired()){
                if(mainConfigManager.isKitPreviewRequiresKitPermission() && !kitModel.playerHasPermission(player)){
                    msgManager.sendMessage(player,messagesConfig.getString("cantPreviewError"),true);
                    return;
                }
            }
            inventoryPlayer.setPreviousInventoryName(inventoryPlayer.getInventoryName());
            inventoryPlayer.setInventoryName("preview_inventory");
            inventoryPlayer.setKitName(kitName);
            openInventory(inventoryPlayer);
            return;
        }


        PlayerKitsMessageResult result = plugin.getKitsManager().giveKit(player,kitName,
                new GiveKitInstructions());
        if(result.isError()){
            msgManager.sendMessage(player,result.getMessage(),true);
            return;
        }else{
            if(result.isProceedToBuy()){
                //Open requirements inventory
                inventoryPlayer.setPreviousInventoryName(inventoryPlayer.getInventoryName());
                inventoryPlayer.setInventoryName("buy_requirements_inventory");
                inventoryPlayer.setKitName(kitName);
                openInventory(inventoryPlayer);
                return;
            }
            msgManager.sendMessage(player,messagesConfig.getString("kitReceived").replace("%kit%",kitName),true);
        }

        if(mainConfigManager.isCloseInventoryOnClaim()){
            player.closeInventory();
            return;
        }

        openInventory(inventoryPlayer);
    }

    public void clickOnOpenInventoryItem(InventoryPlayer inventoryPlayer,String openInventory){
        if(openInventory.equals("previous")){
            inventoryPlayer.setInventoryName(inventoryPlayer.getPreviousInventoryName());
        }else{
            inventoryPlayer.setPreviousInventoryName(inventoryPlayer.getInventoryName());
            inventoryPlayer.setInventoryName(openInventory);
        }
        openInventory(inventoryPlayer);
    }

    public void clickOnCommandItem(InventoryPlayer inventoryPlayer,String itemCommands){
        String[] sep = itemCommands.split("\\|");
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        for(String c : sep) {
            if(c.startsWith("msg %player% ")) {
                inventoryPlayer.getPlayer().sendMessage(MessagesManager.getColoredMessage(c.replace("msg %player% ", "")));
            }else if(c.equals("close_inventory")){
                inventoryPlayer.getPlayer().closeInventory();
            }else{
                Bukkit.dispatchCommand(sender, c.replace("%player%", inventoryPlayer.getPlayer().getName()));
            }
        }
    }

    public void setKit(String kitName, Player player, Inventory inv, int slot, KitsManager kitsManager, PlayerDataManager playerDataManager
        ,KitItemManager kitItemManager,MessagesManager msgManager){
        KitModel kitModel = kitsManager.getKitByName(kitName);
        if(kitModel == null){
            return;
        }

        ArrayList<KitVariable> variablesToReplace = new ArrayList<>();
        variablesToReplace.add(new KitVariable("%kit_name%", kitModel.getName()));

        KitItem kitItem = null;
        if(!kitModel.playerHasPermission(player)){
            kitItem = kitModel.getDisplayItemNoPermission();
        }else{
            //One time
            if(kitModel.isOneTime() && !PlayerUtils.isPlayerKitsAdmin(player) && playerDataManager.isKitOneTime(player, kitModel.getName())){
                kitItem = kitModel.getDisplayItemOneTime();
            }

            //One time requirements
            if(kitModel.getRequirements() != null && kitModel.getRequirements().isOneTimeRequirements()
                && playerDataManager.isKitBought(player, kitModel.getName())){
                kitItem = kitModel.getDisplayItemOneTimeRequirements();
            }

            //Cooldown
            long playerCooldown = playerDataManager.getKitCooldown(player, kitModel.getName());
            if(kitModel.getCooldown() != 0 && !PlayerUtils.isPlayerKitsAdmin(player)){
                String timeStringMillisDif = playerDataManager.getKitCooldownString(playerCooldown);
                if(!timeStringMillisDif.isEmpty()) {
                    kitItem = kitModel.getDisplayItemCooldown();
                    variablesToReplace.add(new KitVariable("%time%",timeStringMillisDif));
                }
            }
        }
        if(kitItem == null){
            kitItem = kitModel.getDisplayItemDefault();
        }

        ItemStack item = kitItemManager.createItemFromKitItem(kitItem,player);
        kitItemManager.replaceVariables(item,variablesToReplace);

        item = ItemUtils.setTagStringItem(plugin,item, "playerkits_kit", kitName);

        inv.setItem(slot,item);
    }



    public KitPosition getKitPositionByKitName(String kitName){
        for(KitInventory kitInventory : inventories){
            for(ItemKitInventory itemKitInventory : kitInventory.getItems()){
                if(itemKitInventory.getType() != null && itemKitInventory.getType().equals("kit: "+kitName)){
                    return new KitPosition(itemKitInventory.getSlots().get(0), kitInventory.getName());
                }
            }
        }
        return null;
    }

    public void removeKitFromInventory(String kitName){
        for(KitInventory inventory : inventories){
            List<ItemKitInventory> items = inventory.getItems();
            for(int i=0;i<items.size();i++){
                ItemKitInventory item = items.get(i);
                if(item.getType() != null && item.getType().equals("kit: "+kitName)){
                    items.remove(i);
                    i--;
                }
            }
        }
    }
}
