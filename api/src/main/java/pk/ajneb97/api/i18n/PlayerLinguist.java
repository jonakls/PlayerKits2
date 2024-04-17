package pk.ajneb97.api.i18n;

import me.yushust.message.language.Linguist;
import org.jetbrains.annotations.Nullable;
import pk.ajneb97.api.model.player.PlayerModel;

public class PlayerLinguist implements Linguist<PlayerModel> {

    @Override
    public @Nullable String getLanguage(PlayerModel playerModel) {
        return "en";
    }
}
