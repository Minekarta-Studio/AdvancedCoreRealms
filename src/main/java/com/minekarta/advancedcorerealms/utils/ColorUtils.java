package com.minekarta.advancedcorerealms.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class ColorUtils {
    
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    
    /**
     * Process a message string using MiniMessage to convert colors, gradients, legacy color codes, and placeholders
     * @param message The message to process
     * @param player The player for placeholder replacement (can be null)
     * @return The processed message with colors and placeholders converted
     */
    public static String processColors(String message, Player player) {
        if (message == null) {
            return null;
        }
        
        // Process placeholders first (if player is provided and PlaceholderAPI is available)
        if (player != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        
        // Use MiniMessage to parse the message (supports & colors, hex colors, gradients, etc.)
        Component component = miniMessage.deserialize(message);
        
        return miniMessage.serialize(component);
    }
    
    /**
     * Process a message string using MiniMessage to convert colors, gradients, and legacy color codes (without placeholders)
     * @param message The message to process
     * @return The processed message with colors converted
     */
    public static String processColors(String message) {
        return processColors(message, null);
    }
    
    /**
     * Convert a String to a Component using MiniMessage
     * @param message The message to convert
     * @return The Component representation of the message
     */
    public static Component toComponent(String message) {
        return miniMessage.deserialize(message);
    }
    
    /**
     * Convert a String to a Component using MiniMessage with a player for placeholders
     * @param message The message to convert
     * @param player The player for placeholder replacement
     * @return The Component representation of the message
     */
    public static Component toComponent(String message, Player player) {
        String processed = processColors(message, player);
        return miniMessage.deserialize(processed);
    }
    
    /**
     * Process a list of strings for colors and placeholders using MiniMessage
     * @param list The list of strings to process
     * @param player The player for placeholder replacement (can be null)
     * @return The processed list with colors and placeholders converted
     */
    public static List<String> processColorList(List<String> list, Player player) {
        if (list == null) return null;
        
        for (int i = 0; i < list.size(); i++) {
            list.set(i, processColors(list.get(i), player));
        }
        
        return list;
    }
    
    /**
     * Process a list of strings for colors (without placeholders) using MiniMessage
     * @param list The list of strings to process
     * @return The processed list with colors converted
     */
    public static List<String> processColorList(List<String> list) {
        return processColorList(list, null);
    }
    
    /**
     * Send a MiniMessage formatted message to a player
     * @param player The player to send the message to
     * @param message The message to send
     */
    public static void sendMessage(Player player, String message) {
        Component component = toComponent(message, player);
        player.sendMessage(component);
    }
    
    /**
     * Send a MiniMessage formatted message to a player with placeholders
     * @param player The player to send the message to
     * @param message The message to send
     * @param placeholders Placeholders to replace in the format "key", "value", "key2", "value2", etc.
     */
    public static void sendMessage(Player player, String message, String... placeholders) {
        Component component = toComponent(message, player);
        
        // Add additional placeholders
        if (placeholders.length > 0) {
            net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.Builder resolverBuilder = 
                net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.builder();
            
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 < placeholders.length) {
                    resolverBuilder.resolver(Placeholder.parsed(placeholders[i], placeholders[i + 1]));
                }
            }
            
            component = miniMessage.deserialize(message, resolverBuilder.build());
            if (player != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                String processedMessage = PlaceholderAPI.setPlaceholders(player, miniMessage.serialize(component));
                component = miniMessage.deserialize(processedMessage);
            }
        }
        
        player.sendMessage(component);
    }
}