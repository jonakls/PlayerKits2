package pk.ajneb97.api.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerUtils {

    public static ItemStack[] getAllInventoryContents(Player player) {
        if (Bukkit.getVersion().contains("1.8")) {
            ItemStack[] contents = new ItemStack[40];

            int slot = 0;
            ItemStack[] normalContents = player.getInventory().getContents();
            for (ItemStack item : normalContents) {
                contents[slot] = item;
                slot++;
            }
            ItemStack[] armorContents = player.getInventory().getArmorContents();
            for (ItemStack item : armorContents) {
                contents[slot] = item;
                slot++;
            }
            return contents;
        } else {
            return player.getInventory().getContents();
        }
    }

    public static int getUsedSlots(Player player) {
        ItemStack[] contents = getAllInventoryContents(player);
        int usedSlots = 0;
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && !contents[i].getType().equals(Material.AIR)) {
                usedSlots++;
            }
        }

        return usedSlots;
    }

    public static boolean isPlayerKitsAdmin(CommandSender sender) {
        return sender.hasPermission("playerkits.admin");
    }

    public static boolean hasCooldownBypassPermission(CommandSender sender) {
        return sender.hasPermission("playerkits.bypass.cooldown");
    }

    public static boolean hasOneTimeBypassPermission(CommandSender sender) {
        return sender.hasPermission("playerkits.bypass.onetime");
    }

}
