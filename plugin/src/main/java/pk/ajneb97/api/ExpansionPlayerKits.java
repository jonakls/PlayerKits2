package pk.ajneb97.api;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExpansionPlayerKits extends PlaceholderExpansion {

    private final Plugin plugin;

    public ExpansionPlayerKits(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return "Ajneb97 & Jonakls";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "playerkits";
    }

    @Override
    @SuppressWarnings({"UnstableApiUsage"})
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(@NotNull Player player, @NotNull String identifier) {

        if (identifier.startsWith("cooldown_")) {
            // %playerkits_cooldown_<kit>%
            String kitName = identifier.replace("cooldown_", "");
            return null; // TODO: Implement this
        }

        if (identifier.startsWith("onetime_ready_")) {
            // %conditionalevents_onetime_ready_<kit>%
            String kitName = identifier.replace("onetime_ready_", "");
            return null; // TODO: Implement this
        }

        return null;
    }
}
