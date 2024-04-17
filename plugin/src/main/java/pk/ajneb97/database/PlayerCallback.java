package pk.ajneb97.database;

import pk.ajneb97.api.model.player.PlayerModel;

public interface PlayerCallback {

    void onDone(PlayerModel player);
}
