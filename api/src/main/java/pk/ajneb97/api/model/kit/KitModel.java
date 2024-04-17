package pk.ajneb97.api.model.kit;

import org.bukkit.command.CommandSender;
import pk.ajneb97.api.model.Model;
import pk.ajneb97.api.model.kit.item.KitItem;

import java.util.ArrayList;
import java.util.List;

public class KitModel implements Model {
    private String name;
    private int cooldown;
    private boolean permissionRequired;
    private boolean oneTime;
    private List<KitItem> items;
    private List<KitAction> claimActions;
    private List<KitAction> errorActions;

    private KitItem displayItemDefault;
    private KitItem displayItemNoPermission;
    private KitItem displayItemCooldown;
    private KitItem displayItemOneTime;
    private KitItem displayItemOneTimeRequirements;
    private KitRequirements requirements;

    private boolean autoArmor;

    public KitModel(String name) {
        this.name = name;
        this.cooldown = 0;
        this.autoArmor = false;
        this.oneTime = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }


    public boolean isPermissionRequired() {
        return permissionRequired;
    }

    public void setPermissionRequired(boolean permissionRequired) {
        this.permissionRequired = permissionRequired;
    }

    public List<KitItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<KitItem> items) {
        this.items = items;
    }

    public KitItem getDisplayItemDefault() {
        return displayItemDefault;
    }

    public void setDisplayItemDefault(KitItem displayItemDefault) {
        this.displayItemDefault = displayItemDefault;
    }

    public KitItem getDisplayItemNoPermission() {
        return displayItemNoPermission;
    }

    public void setDisplayItemNoPermission(KitItem displayItemNoPermission) {
        this.displayItemNoPermission = displayItemNoPermission;
    }

    public KitItem getDisplayItemCooldown() {
        return displayItemCooldown;
    }

    public void setDisplayItemCooldown(KitItem displayItemCooldown) {
        this.displayItemCooldown = displayItemCooldown;
    }

    public boolean isAutoArmor() {
        return autoArmor;
    }

    public void setAutoArmor(boolean autoArmor) {
        this.autoArmor = autoArmor;
    }

    public boolean isOneTime() {
        return oneTime;
    }

    public void setOneTime(boolean oneTime) {
        this.oneTime = oneTime;
    }

    public List<KitAction> getClaimActions() {
        return claimActions;
    }

    public void setClaimActions(ArrayList<KitAction> claimActions) {
        this.claimActions = claimActions;
    }

    public List<KitAction> getErrorActions() {
        return errorActions;
    }

    public void setErrorActions(ArrayList<KitAction> errorActions) {
        this.errorActions = errorActions;
    }

    public KitItem getDisplayItemOneTime() {
        return displayItemOneTime;
    }

    public void setDisplayItemOneTime(KitItem displayItemOneTime) {
        this.displayItemOneTime = displayItemOneTime;
    }

    public KitItem getDisplayItemOneTimeRequirements() {
        return displayItemOneTimeRequirements;
    }

    public void setDisplayItemOneTimeRequirements(KitItem displayItemOneTimeRequirements) {
        this.displayItemOneTimeRequirements = displayItemOneTimeRequirements;
    }

    public KitRequirements getRequirements() {
        return requirements;
    }

    public void setRequirements(KitRequirements requirements) {
        this.requirements = requirements;
    }

    public boolean playerHasPermission(CommandSender player) {
        if (permissionRequired) {
            return player.hasPermission("playerkits.admin") || player.hasPermission("playerkits.kit." + name);
        }
        return true;
    }

    public void setDefaults(KitModel defaultKitModel) {
        cooldown = defaultKitModel.getCooldown();
        oneTime = defaultKitModel.isOneTime();
        permissionRequired = defaultKitModel.isPermissionRequired();
        if (defaultKitModel.getClaimActions() != null) {
            ArrayList<KitAction> actions = new ArrayList<>();
            for (KitAction action : defaultKitModel.getClaimActions()) {
                actions.add(action.clone());
            }
            claimActions = actions;
        }
        if (defaultKitModel.getErrorActions() != null) {
            ArrayList<KitAction> actions = new ArrayList<>();
            for (KitAction action : defaultKitModel.getErrorActions()) {
                actions.add(action.clone());
            }
            errorActions = actions;
        }
        displayItemDefault = defaultKitModel.getDisplayItemDefault() != null ? defaultKitModel.getDisplayItemDefault().clone() : null;
        displayItemNoPermission = defaultKitModel.getDisplayItemNoPermission() != null ? defaultKitModel.getDisplayItemNoPermission().clone() : null;
        displayItemCooldown = defaultKitModel.getDisplayItemCooldown() != null ? defaultKitModel.getDisplayItemCooldown().clone() : null;
        displayItemOneTime = defaultKitModel.getDisplayItemOneTime() != null ? defaultKitModel.getDisplayItemOneTime().clone() : null;
        displayItemOneTimeRequirements = defaultKitModel.getDisplayItemOneTimeRequirements() != null ? defaultKitModel.getDisplayItemOneTimeRequirements().clone() : null;
        autoArmor = defaultKitModel.isAutoArmor();
    }

    @Override
    public String id() {
        return this.name;
    }
}
