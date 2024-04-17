package top.jonakls.playerkits.manager;

import me.yushust.message.MessageHandler;
import me.yushust.message.bukkit.BukkitMessageAdapt;
import me.yushust.message.source.MessageSourceDecorator;
import org.bukkit.plugin.Plugin;
import top.jonakls.playerkits.api.manager.Manager;

public class MessageManager implements Manager<MessageHandler> {

    private final Plugin plugin;

    public MessageManager(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public MessageHandler get() {
        return MessageHandler.of(
                MessageSourceDecorator.decorate(BukkitMessageAdapt.newYamlSource(this.plugin, "I18n/%lang%.yml"))
                        .addFallbackLanguage("en").get(),
                config -> {
                }
        );
    }
}
