package pk.ajneb97.utils;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import top.jonakls.playerkits.api.util.TextUtil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ActionUtils {

    public static void consoleCommand(String actionLine) {
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(sender, actionLine);
    }

    public static void playerCommand(Player player, String actionLine) {
        player.performCommand(actionLine);
    }

    public static void playSound(Player player, String actionLine, ComponentLogger logger) {
        String[] sep = actionLine.split(";");
        Sound sound;
        int volume;
        float pitch;
        try {
            sound = Sound.valueOf(sep[0]);
            volume = Integer.parseInt(sep[1]);
            pitch = Float.parseFloat(sep[2]);
        } catch (Exception e) {
            logger.warn(TextUtil.legacyColor("&7Sound Name: &c" + sep[0] + " &7is not valid. Change it in the config!"));
            return;
        }

        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static void actionbar(Player player, String actionLine) {
        String[] sep = actionLine.split(";");
        String text = sep[0];
        // int duration = Integer.parseInt(sep[1]);

        player.sendActionBar(TextUtil.legacyColor(text));
    }

    public static void title(Player player, String actionLine) {
        String[] sep = actionLine.split(";");
        int fadeIn = Integer.parseInt(sep[0]);
        int stay = Integer.parseInt(sep[1]);
        int fadeOut = Integer.parseInt(sep[2]);

        String title = sep[3];
        String subtitle = sep[4];
        if (title.equals("none")) {
            title = "";
        }
        if (subtitle.equals("none")) {
            subtitle = "";
        }

        Title componentTitle = Title.title(
                TextUtil.legacyColor(title), TextUtil.legacyColor(subtitle),
                Title.Times.times(
                        Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut)
                )
        );

        player.showTitle(componentTitle);
    }

    public static void firework(Player player, String actionLine, Plugin plugin) {
        List<Color> colors = new ArrayList<>();
        FireworkEffect.Type type = null;
        List<Color> fadeColors = new ArrayList<>();
        int power = 0;

        String[] splitValues = actionLine.split(" ");
        for (String value : splitValues) {

            if (value.startsWith("colors:")) {
                value = value.replace("colors:", "");
                String[] colorsSep = value.split(",");
                for (String colorSep : colorsSep) {
                    colors.add(OtherUtils.getFireworkColorFromName(colorSep));
                }
            } else if (value.startsWith("type:")) {
                value = value.replace("type:", "");
                type = FireworkEffect.Type.valueOf(value);
            } else if (value.startsWith("fade:")) {
                value = value.replace("fade:", "");
                String[] colorsSep = value.split(",");
                for (String colorSep : colorsSep) {
                    fadeColors.add(OtherUtils.getFireworkColorFromName(colorSep));
                }
            } else if (value.startsWith("power:")) {
                value = value.replace("power:", "");
                power = Integer.valueOf(value);
            }
        }

        Location location = player.getLocation();
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder()
                .flicker(false)
                .withColor(colors)
                .with(type)
                .withFade(fadeColors)
                .build();

        fireworkMeta.addEffect(effect);
        fireworkMeta.setPower(power);
        firework.setFireworkMeta(fireworkMeta);
        firework.setMetadata(
                "playerkits", new FixedMetadataValue(plugin, "no_damage")
        );
    }
}
