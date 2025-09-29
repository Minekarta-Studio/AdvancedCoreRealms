package com.minekarta.advancedcorerealms.utils;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class MessageUtils {

    public static void sendMessage(Player player, String key) {
        String message = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage(key);
        String prefix = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage("prefix");
        ColorUtils.sendMessage(player, prefix + message);
    }

    public static void sendMessage(Player player, String key, String... placeholders) {
        String message = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage(key, placeholders);
        String prefix = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage("prefix");
        ColorUtils.sendMessage(player, prefix + message, placeholders);
    }

    public static Component formatMessage(String key) {
        String message = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage(key);
        String prefix = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage("prefix");
        return ColorUtils.toComponent(prefix + message);
    }

    public static Component formatMessage(String key, String... placeholders) {
        String message = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage(key, placeholders);
        String prefix = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage("prefix");
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        return ColorUtils.toComponent(prefix + message);
    }

    public static Component formatMessage(Player player, String key) {
        String message = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage(key);
        String prefix = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage("prefix");
        return ColorUtils.toComponent(prefix + message, player);
    }

    public static Component formatMessage(Player player, String key, String... placeholders) {
        String message = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage(key, placeholders);
        String prefix = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage("prefix");
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        return ColorUtils.toComponent(prefix + message, player);
    }
}