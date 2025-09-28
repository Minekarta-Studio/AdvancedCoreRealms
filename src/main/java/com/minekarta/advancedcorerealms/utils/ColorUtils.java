package com.minekarta.advancedcorerealms.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ColorUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private ColorUtils() {
        // Utility class
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(toComponent(message, player));
    }

    public static void sendMessage(Player player, String message, String... placeholders) {
        String formattedMessage = message;
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                formattedMessage = formattedMessage.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        sendMessage(player, formattedMessage);
    }

    public static String processColors(String message) {
        return processColors(message, null);
    }

    public static String processColors(String message, Player player) {
        if (message == null) {
            return "";
        }
        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        return miniMessage.serialize(miniMessage.deserialize(message));
    }

    public static Component toComponent(String message) {
        return toComponent(message, null);
    }

    public static Component toComponent(String message, Player player) {
        if (message == null) {
            return Component.empty();
        }
        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        return miniMessage.deserialize(message);
    }
}