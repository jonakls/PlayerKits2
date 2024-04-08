package pk.ajneb97.database;

import pk.ajneb97.api.model.player.PlayerData;

public interface PlayerCallback {

    void onDone(PlayerData player);
}
