package pk.ajneb97.managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.api.model.kit.KitModel;
import pk.ajneb97.configs.MainConfigManager;
import pk.ajneb97.api.model.kit.KitAction;
import pk.ajneb97.api.model.kit.KitRequirements;
import pk.ajneb97.api.model.kit.GiveKitInstructions;
import pk.ajneb97.api.model.player.PlayerKitsMessageResult;
import pk.ajneb97.api.model.gui.KitInventory;
import pk.ajneb97.api.model.kit.item.KitItem;
import pk.ajneb97.utils.ActionUtils;
import pk.ajneb97.utils.OtherUtils;
import pk.ajneb97.api.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

public class KitsManager {

    private PlayerKits2 plugin;
    private ArrayList<KitModel> kitModels;

    public KitsManager(PlayerKits2 plugin) {
        this.plugin = plugin;
    }

    public PlayerKits2 getPlugin() {
        return plugin;
    }

    public void setPlugin(PlayerKits2 plugin) {
        this.plugin = plugin;
    }

    public ArrayList<KitModel> getKits() {
        return kitModels;
    }

    public void setKits(ArrayList<KitModel> kitModels) {
        this.kitModels = kitModels;
    }

    public KitModel getKitByName(String name) {
        for (KitModel kitModel : kitModels) {
            if (kitModel.getName().equals(name)) {
                return kitModel;
            }
        }
        return null;
    }

    public void removeKit(String name) {
        for (int i = 0; i < kitModels.size(); i++) {
            if (kitModels.get(i).getName().equals(name)) {
                kitModels.remove(i);
                return;
            }
        }
    }

    public void createKit(String kitName, Player player) {
        KitModel kitModel = getKitByName(kitName);
        FileConfiguration messagesFile = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MainConfigManager mainConfigManager = plugin.getConfigsManager().getMainConfigManager();
        MessagesManager msgManager = plugin.getMessagesManager();
        if (kitModel != null) {
            msgManager.sendMessage(player, messagesFile.getString("kitAlreadyExists").replace("%kit%", kitName), true);
            return;
        }

        ItemStack[] inventoryContents = PlayerUtils.getAllInventoryContents(player);

        KitItemManager kitItemManager = plugin.getKitItemManager();
        ArrayList<KitItem> items = new ArrayList<>();
        boolean hasArmor = false;
        for (int i = 0; i < inventoryContents.length; i++) {
            ItemStack item = inventoryContents[i];
            if (item == null || item.getType().equals(Material.AIR)) {
                continue;
            }

            KitItem kitItem = kitItemManager.createKitItemFromItemStack(item);

            //Check for armor/offhand
            if (i >= 36 && i <= 39) {
                hasArmor = true;
            }
            if (i == 40) {
                kitItem.setOffhand(true);
            }

            items.add(kitItem);
        }

        if (items.isEmpty()) {
            msgManager.sendMessage(player, messagesFile.getString("inventoryEmpty"), true);
            return;
        }

        //Set defaults
        kitModel = new KitModel(kitName);
        kitModel.setItems(items);
        kitModel.setDefaults(mainConfigManager.getNewKitDefault());
        kitModel.setAutoArmor(hasArmor);

        kitModels.add(kitModel);
        plugin.getConfigsManager().getKitsConfigManager().saveConfig(kitModel);

        msgManager.sendMessage(player, messagesFile.getString("kitCreated").replace("%kit%", kitName), true);

        //Add on inventory
        InventoryManager inventoryManager = plugin.getInventoryManager();
        String newKitDefaultInventory = mainConfigManager.getNewKitDefaultInventory();
        KitInventory inventory = inventoryManager.getInventory(newKitDefaultInventory);
        if (inventory != null) {
            int resultSlot = inventory.addKitItemOnFirstEmptySlot(kitName);
            if (resultSlot == -1) {
                msgManager.sendMessage(player, messagesFile.getString("kitNotAddedToInventory"), true);
            } else {
                msgManager.sendMessage(player, messagesFile.getString("kitAddedToInventory")
                        .replace("%inventory%", newKitDefaultInventory).replace("%slot%", resultSlot + ""), true);
                //Update inventory file
                plugin.getConfigsManager().getInventoryConfigManager().saveKitItemOnConfig(newKitDefaultInventory, resultSlot, kitName);
            }
        }
    }

    public void deleteKit(String kitName, CommandSender sender) {
        FileConfiguration messagesFile = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MessagesManager msgManager = plugin.getMessagesManager();
        if (getKitByName(kitName) == null) {
            msgManager.sendMessage(sender, messagesFile.getString("kitDoesNotExists")
                    .replace("%kit%", kitName), true);
            return;
        }

        removeKit(kitName);
        plugin.getConfigsManager().getKitsConfigManager().removeKitFile(kitName);
        plugin.getInventoryManager().removeKitFromInventory(kitName);
        plugin.getConfigsManager().getInventoryConfigManager().save();

        msgManager.sendMessage(sender, messagesFile.getString("kitDeleted").replace("%kit%", kitName), true);
    }

    public PlayerKitsMessageResult giveKit(Player player, String kitName, GiveKitInstructions giveKitInstructions) {
        KitModel kitModel = getKitByName(kitName);
        FileConfiguration messagesFile = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        FileConfiguration configFile = plugin.getConfigsManager().getMainConfigManager().getConfig();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        MessagesManager msgManager = plugin.getMessagesManager();

        if (kitModel == null) {
            return PlayerKitsMessageResult.error(messagesFile.getString("kitDoesNotExists").replace("%kit%", kitName));
        }

        //Check properties
        if (!giveKitInstructions.isFromCommand()) {
            //Permission
            if (!giveKitInstructions.isIgnorePermission() && !kitModel.playerHasPermission(player)) {
                sendKitActions(kitModel.getErrorActions(), player, false);
                return PlayerKitsMessageResult.error(messagesFile.getString("kitNoPermissions"));
            }

            //One time
            if (kitModel.isOneTime() && !PlayerUtils.isPlayerKitsAdmin(player) && !PlayerUtils.hasOneTimeBypassPermission(player)
                    && playerDataManager.isKitOneTime(player, kitModel.getName())) {
                sendKitActions(kitModel.getErrorActions(), player, false);
                return PlayerKitsMessageResult.error(messagesFile.getString("oneTimeError"));
            }

            //Cooldown
            long playerCooldown = playerDataManager.getKitCooldown(player, kitModel.getName());
            if (kitModel.getCooldown() != 0 && !PlayerUtils.isPlayerKitsAdmin(player) && !PlayerUtils.hasCooldownBypassPermission(player)) {
                long currentMillis = System.currentTimeMillis();
                long millisDif = playerCooldown - currentMillis;
                //String timeStringMillisDif = OtherUtils.getTime(millisDif / 1000, msgManager);
                String timeStringMillisDif = OtherUtils.getTime(millisDif, msgManager);
                if (!timeStringMillisDif.isEmpty()) {
                    sendKitActions(kitModel.getErrorActions(), player, false);
                    return PlayerKitsMessageResult.error(messagesFile.getString("cooldownError")
                            .replace("%time%", timeStringMillisDif));
                }
            }

            //Requirements - Buy
            KitRequirements kitRequirements = kitModel.getRequirements();
            if (!giveKitInstructions.isIgnoreRequirements() && kitRequirements != null &&
                    (kitRequirements.getPrice() != 0 || !kitRequirements.getExtraRequirements().isEmpty())) {
                if (!(kitRequirements.isOneTimeRequirements() && playerDataManager.isKitBought(player, kitModel.getName()))) {
                    if (!giveKitInstructions.isRequirementsSatisfied()) {
                        //Player must buy it first
                        PlayerKitsMessageResult result = PlayerKitsMessageResult.success();
                        result.setProceedToBuy(true);
                        return result;
                    }

                    //Check price
                    if (!passPrice(kitRequirements.getPrice(), player)) {
                        sendKitActions(kitModel.getErrorActions(), player, false);
                        return PlayerKitsMessageResult.error(messagesFile.getString("requirementsError"));
                    }
                    //Check requirements
                    List<String> requirementsConditions = kitRequirements.getExtraRequirements();
                    if (plugin.getDependencyManager().isPlaceholderAPI()) {
                        for (String condition : requirementsConditions) {
                            boolean passCondition = pk.ajneb97.utils.PlayerUtils.passCondition(player, condition);
                            if (!passCondition) {
                                sendKitActions(kitModel.getErrorActions(), player, false);
                                return PlayerKitsMessageResult.error(messagesFile.getString("requirementsError"));
                            }
                        }
                    }
                }
            }
        }


        KitItemManager kitItemManager = plugin.getKitItemManager();
        List<KitItem> items = kitModel.getItems();

        //Check amount of free slots, including auto-armor
        int usedSlots = PlayerUtils.getUsedSlots(player);
        int freeSlots = 36 - usedSlots;
        int inventoryKitItems = 0; //Items that will be put in the player inventory (not equipment)

        KitItem itemHelmet = null;
        KitItem itemChestplate = null;
        KitItem itemLeggings = null;
        KitItem itemBoots = null;
        KitItem itemOffhand = null;

        PlayerInventory playerInventory = player.getInventory();
        for (KitItem item : items) {
            if (kitModel.isAutoArmor()) {
                String id = item.getId();

                //Check if the item must be put in the player equipment
                if ((id.contains("_HELMET") || id.contains("PLAYER_HEAD") || id.contains("SKULL_ITEM")) && itemHelmet == null && (playerInventory.getHelmet() == null
                        || playerInventory.getHelmet().getType().equals(Material.AIR))) {
                    itemHelmet = item;
                    continue;
                } else if ((id.contains("_CHESTPLATE") || id.contains("ELYTRA")) && itemChestplate == null && (playerInventory.getChestplate() == null
                        || playerInventory.getChestplate().getType().equals(Material.AIR))) {
                    itemChestplate = item;
                    continue;
                } else if (id.contains("_LEGGINGS") && itemLeggings == null && (playerInventory.getLeggings() == null
                        || playerInventory.getLeggings().getType().equals(Material.AIR))) {
                    itemLeggings = item;
                    continue;
                } else if (id.contains("_BOOTS") && itemBoots == null && (playerInventory.getBoots() == null
                        || playerInventory.getBoots().getType().equals(Material.AIR))) {
                    itemBoots = item;
                    continue;
                }
            }

            if (item.isOffhand() && itemOffhand == null && (playerInventory.getItemInOffHand() == null
                    || playerInventory.getItemInOffHand().getType().equals(Material.AIR))) {
                itemOffhand = item;
                continue;
            }

            inventoryKitItems++;
        }
        List<KitAction> claimActions = kitModel.getClaimActions();
        for (KitAction action : claimActions) {
            if (action.isCountAsItem()) {
                inventoryKitItems++;
            }
        }


        boolean enoughSpace = freeSlots < inventoryKitItems;
        boolean dropItemsIfFullInventory = configFile.getBoolean("drop_items_if_full_inventory");

        if (enoughSpace && !dropItemsIfFullInventory) {
            sendKitActions(kitModel.getErrorActions(), player, false);
            return PlayerKitsMessageResult.error(messagesFile.getString("noSpaceError"));
        }

        //Actions before
        sendKitActions(kitModel.getClaimActions(), player, true);

        //Give kit items
        for (KitItem kitItem : items) {
            ItemStack item = kitItemManager.createItemFromKitItem(kitItem, player);

            if (itemHelmet != null && kitItem.equals(itemHelmet)) {
                playerInventory.setHelmet(item);
            } else if (itemChestplate != null && kitItem.equals(itemChestplate)) {
                playerInventory.setChestplate(item);
            } else if (itemLeggings != null && kitItem.equals(itemLeggings)) {
                playerInventory.setLeggings(item);
            } else if (itemBoots != null && kitItem.equals(itemBoots)) {
                playerInventory.setBoots(item);
            } else if (itemOffhand != null && kitItem.equals(itemOffhand)) {
                playerInventory.setItemInOffHand(item);
            } else {
                if (playerInventory.firstEmpty() == -1 && dropItemsIfFullInventory) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                } else {
                    playerInventory.addItem(item);
                }
            }
        }

        //Actions after
        sendKitActions(kitModel.getClaimActions(), player, false);

        //Update properties
        if (!giveKitInstructions.isFromCommand()) {
            //One time
            if (kitModel.isOneTime() && !PlayerUtils.isPlayerKitsAdmin(player) && !PlayerUtils.hasOneTimeBypassPermission(player)) {
                playerDataManager.setKitOneTime(player, kitModel.getName());
            }

            //Cooldown
            if (kitModel.getCooldown() != 0 && !PlayerUtils.isPlayerKitsAdmin(player) && !PlayerUtils.hasCooldownBypassPermission(player)) {
                long millisMax = System.currentTimeMillis() + (kitModel.getCooldown() * 1000L);
                playerDataManager.setKitCooldown(player, kitModel.getName(), millisMax);
            }

            //Requirements - Buy
            KitRequirements kitRequirements = kitModel.getRequirements();
            if (!giveKitInstructions.isIgnoreRequirements() && kitRequirements != null && giveKitInstructions.isRequirementsSatisfied()) {
                //Check price and update balance
                double price = kitRequirements.getPrice();
                Economy economy = plugin.getDependencyManager().getVaultEconomy();
                if (price > 0 && economy != null) {
                    economy.withdrawPlayer(player, price);
                }

                //Actions
                List<String> actions = kitRequirements.getActionsOnBuy();
                for (String action : actions) {
                    executeAction(player, action);
                }

                //Data
                if (kitRequirements.isOneTimeRequirements()) {
                    playerDataManager.setKitBought(player, kitName);
                }
            }
        }

        return PlayerKitsMessageResult.success();
    }

    public void giveFirstJoinKit(Player player) {
        // Will ignore:
        // - requirements
        // - permissions
        String firstJoinKit = plugin.getConfigsManager().getMainConfigManager().getFirstJoinKit();
        if (firstJoinKit.equals("none")) {
            return;
        }
        giveKit(player, firstJoinKit, new GiveKitInstructions(false, false, true, true));
    }

    public void executeAction(Player player, String actionText) {
        int indexFirst = actionText.indexOf(" ");
        String actionType = actionText.substring(0, indexFirst).replace(":", "");
        String actionLine = actionText.substring(indexFirst + 1);
        actionLine = OtherUtils.replaceGlobalVariables(actionLine, player, plugin);

        switch (actionType) {
            case "console_command":
                ActionUtils.consoleCommand(actionLine);
                break;
            case "player_command":
                ActionUtils.playerCommand(player, actionLine);
                break;
            case "playsound":
                ActionUtils.playSound(player, actionLine, this.plugin.getComponentLogger());
                break;
            case "actionbar":
                ActionUtils.actionbar(player, actionLine);
                break;
            case "title":
                ActionUtils.title(player, actionLine);
                break;
            case "firework":
                ActionUtils.firework(player, actionLine, plugin);
                break;
        }
    }

    public void sendKitActions(List<KitAction> actions, Player player, boolean beforeItems) {
        for (KitAction action : actions) {
            if (action.isExecuteBeforeItems() == beforeItems) {
                String actionText = action.getAction();
                executeAction(player, actionText);
            }
        }
    }

    public boolean passPrice(double price, Player player) {
        if (price != 0) {
            Economy economy = plugin.getDependencyManager().getVaultEconomy();
            if (economy != null) {
                if (economy.getBalance(player) < price) {
                    return false;
                }
            }
        }
        return true;
    }

}
