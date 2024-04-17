package pk.ajneb97.api.model.gui;

import pk.ajneb97.api.model.kit.item.KitItem;

import java.util.ArrayList;
import java.util.List;

public class ItemKitInventory {
    private List<Integer> slots;
    private String slotsString;
    private KitItem item;
    private String openInventory;
    private List<String> commands;
    private String type;

    public ItemKitInventory(String slotsString, KitItem item, String openInventory, List<String> commands, String type) {
        slots = new ArrayList<>();
        this.slotsString = slotsString;
        String[] slotsSep = slotsString.split(";");
        for (String s : slotsSep) {
            if (s.contains("-")) {
                String[] newSep = s.split("-");
                int sMin = Integer.parseInt(newSep[0]);
                int sMax = Integer.parseInt(newSep[1]);
                for (int c = sMin; c <= sMax; c++) {
                    slots.add(c);
                }
            } else {
                slots.add(Integer.valueOf(s));
            }
        }

        this.item = item;
        this.openInventory = openInventory;
        this.commands = commands;
        this.type = type;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public void setSlots(List<Integer> slots) {
        this.slots = slots;
    }

    public KitItem getItem() {
        return item;
    }

    public void setItem(KitItem item) {
        this.item = item;
    }

    public String getOpenInventory() {
        return openInventory;
    }

    public void setOpenInventory(String openInventory) {
        this.openInventory = openInventory;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSlotsString() {
        return slotsString;
    }

    public void setSlotsString(String slotsString) {
        this.slotsString = slotsString;
    }
}
