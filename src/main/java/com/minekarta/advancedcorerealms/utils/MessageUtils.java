package com.minekarta.advancedcorerealms.utils;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtils {
    
    public static void sendMessage(Player player, String key) {
        String message = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage(key);
        String prefix = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage("prefix");
        
        player.sendMessage(prefix + message);
    }
    
    public static void sendMessage(Player player, String key, String... placeholders) {
        String message = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage(key, placeholders);
        String prefix = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage("prefix");
        
        player.sendMessage(prefix + message);
    }
    
    public static String formatMessage(String key) {
        String message = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage(key);
        String prefix = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage("prefix");
        
        return prefix + message;
    }
    
    public static String formatMessage(String key, String... placeholders) {
        String message = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage(key, placeholders);
        String prefix = AdvancedCoreRealms.getInstance().getLanguageManager().getMessage("prefix");
        
        return prefix + message;
    }
}