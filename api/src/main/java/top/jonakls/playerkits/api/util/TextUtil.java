package top.jonakls.playerkits.api.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class TextUtil {

    private TextUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Component legacyColor(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    public static String legacyStrip(Component component) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }

    public static String plainText(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
