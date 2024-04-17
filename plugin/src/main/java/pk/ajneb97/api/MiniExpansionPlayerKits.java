package pk.ajneb97.api;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.entity.Player;

public class MiniExpansionPlayerKits {

    public MiniExpansionPlayerKits() {
        this.setup();
    }

    public void setup() {
        final Expansion expansion = Expansion.builder("playerkits")
                .filter(Player.class)
                .audiencePlaceholder("cooldown", (audience, ctx, queue) -> {
                    final Player player = (Player) audience;
                    // TODO: Implement your placeholder here
                    return Tag.selfClosingInserting(Component.text());
                })
                .audiencePlaceholder("onetime_ready", (audience, ctx, queue) -> {
                    final Player player = (Player) audience;
                    return Tag.selfClosingInserting(Component.text());
                })
                .build();

        expansion.register();
    }
}
