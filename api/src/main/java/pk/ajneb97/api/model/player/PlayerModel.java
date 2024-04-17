package pk.ajneb97.api.model.player;

import pk.ajneb97.api.model.Model;

import java.util.ArrayList;
import java.util.List;

public class PlayerModel implements Model {

    private String name;
    private String uuid;

    private List<PlayerDataKit> kits;
    private boolean modified;

    public PlayerModel(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
        this.kits = new ArrayList<>();
        this.modified = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<PlayerDataKit> getKits() {
        return kits;
    }

    public void setKits(ArrayList<PlayerDataKit> kits) {
        this.kits = kits;
    }

    public void addKit(PlayerDataKit kit) {
        this.kits.add(kit);
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public PlayerDataKit getKit(String kitName) {
        for (PlayerDataKit kit : kits) {
            if (kit.getName().equals(kitName)) {
                return kit;
            }
        }
        return null;
    }

    public boolean setKitCooldown(String kitName, long cooldown) {
        PlayerDataKit playerDataKit = getKit(kitName);
        boolean creating = false;
        if (playerDataKit == null) {
            playerDataKit = new PlayerDataKit(kitName);
            kits.add(playerDataKit);
            creating = true;
        }

        playerDataKit.setCooldown(cooldown);
        return creating;
    }

    public long getKitCooldown(String kitName) {
        PlayerDataKit playerDataKit = getKit(kitName);
        if (playerDataKit == null) {
            return 0;
        } else {
            return playerDataKit.getCooldown();
        }
    }

    public boolean setKitOneTime(String kitName) {
        PlayerDataKit playerDataKit = getKit(kitName);
        boolean creating = false;
        if (playerDataKit == null) {
            playerDataKit = new PlayerDataKit(kitName);
            kits.add(playerDataKit);
            creating = true;
        }

        playerDataKit.setOneTime(true);
        return creating;
    }

    public boolean getKitOneTime(String kitName) {
        PlayerDataKit playerDataKit = getKit(kitName);
        if (playerDataKit == null) {
            return false;
        } else {
            return playerDataKit.isOneTime();
        }
    }

    public boolean setKitBought(String kitName) {
        PlayerDataKit playerDataKit = getKit(kitName);
        boolean creating = false;
        if (playerDataKit == null) {
            playerDataKit = new PlayerDataKit(kitName);
            kits.add(playerDataKit);
            creating = true;
        }

        playerDataKit.setBought(true);
        return creating;
    }

    public boolean getKitHasBought(String kitName) {
        PlayerDataKit playerDataKit = getKit(kitName);
        if (playerDataKit == null) {
            return false;
        } else {
            return playerDataKit.isBought();
        }
    }

    public void resetKit(String kitName) {
        this.kits.stream().findAny().ifPresent(playerDataKit -> {
            if (playerDataKit.getName().equals(kitName)) {
                this.kits.remove(playerDataKit);
            }
        });
    }

    @Override
    public String id() {
        return this.uuid;
    }
}