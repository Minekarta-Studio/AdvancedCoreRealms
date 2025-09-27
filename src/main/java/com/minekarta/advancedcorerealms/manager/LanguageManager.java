package com.minekarta.advancedcorerealms.manager;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    
    private final AdvancedCoreRealms plugin;
    private final Map<String, FileConfiguration> languageConfigs;
    private String currentLanguage;
    
    public LanguageManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.languageConfigs = new HashMap<>();
    }
    
    public void loadLanguage() {
        // Create languages directory if it doesn't exist
        File languagesDir = new File(plugin.getDataFolder(), "languages");
        if (!languagesDir.exists()) {
            languagesDir.mkdirs();
        }
        
        // Load the configured language
        this.currentLanguage = plugin.getConfig().getString("language", "en");
        
        // Load language files
        loadLanguageFile("en");
        loadLanguageFile("es");
        loadLanguageFile("id");
        
        // Create default language file if it doesn't exist
        createDefaultLanguageFile();
    }
    
    private void loadLanguageFile(String langCode) {
        File languageFile = new File(plugin.getDataFolder(), "languages/" + langCode + ".yml");
        if (languageFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(languageFile);
            languageConfigs.put(langCode, config);
        }
    }
    
    private void createDefaultLanguageFile() {
        String[] languages = {"en", "es", "id"};
        
        for (String lang : languages) {
            File languageFile = new File(plugin.getDataFolder(), "languages/" + lang + ".yml");
            
            if (!languageFile.exists()) {
                try {
                    languageFile.createNewFile();
                    
                    // Set default language content based on the language
                    FileConfiguration config = YamlConfiguration.loadConfiguration(languageFile);
                    
                    // English defaults
                    if (lang.equals("en")) {
                        config.set("prefix", "&8[&bRealms&8] &r");
                        config.set("command.help", "&aShowing help for AdvancedCoreRealms...");
                        config.set("command.reloaded", "&aConfiguration reloaded successfully.");
                        config.set("world.created", "&aSuccessfully created your new realm named &e%world%&a.");
                        config.set("world.deleted", "&cSuccessfully deleted your realm &e%world%&c.");
                        config.set("world.teleport", "&7Teleporting you to &e%world%&7...");
                        config.set("error.no-permission", "&cYou do not have permission to use this command.");
                        config.set("error.world-exists", "&cA realm with that name already exists.");
                        config.set("error.not-owner", "&cYou are not the owner of this realm.");
                    }
                    // Spanish defaults
                    else if (lang.equals("es")) {
                        config.set("prefix", "&8[&bReinos&8] &r");
                        config.set("command.help", "&aMostrando ayuda para AdvancedCoreRealms...");
                        config.set("command.reloaded", "&aConfiguración recargada exitosamente.");
                        config.set("world.created", "&aSe creó exitosamente tu nuevo reino llamado &e%world%&a.");
                        config.set("world.deleted", "&cSe eliminó exitosamente tu reino &e%world%&c.");
                        config.set("world.teleport", "&7Teletransportándote a &e%world%&7...");
                        config.set("error.no-permission", "&cNo tienes permiso para usar este comando.");
                        config.set("error.world-exists", "&cYa existe un reino con ese nombre.");
                        config.set("error.not-owner", "&cNo eres el propietario de este reino.");
                    }
                    // Indonesian defaults
                    else if (lang.equals("id")) {
                        config.set("prefix", "&8[&bRealms&8] &r");
                        config.set("command.help", "&aMenampilkan bantuan untuk AdvancedCoreRealms...");
                        config.set("command.reloaded", "&aKonfigurasi berhasil dimuat ulang.");
                        config.set("world.created", "&aBerhasil membuat realm baru bernama &e%world%&a.");
                        config.set("world.deleted", "&cBerhasil menghapus realm &e%world%&c.");
                        config.set("world.teleport", "&7Teleportasi ke &e%world%&7...");
                        config.set("error.no-permission", "&cAnda tidak memiliki izin untuk menggunakan perintah ini.");
                        config.set("error.world-exists", "&cRealm dengan nama tersebut sudah ada.");
                        config.set("error.not-owner", "&cAnda bukan pemilik dari realm ini.");
                    }
                    
                    config.save(languageFile);
                    languageConfigs.put(lang, config);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not create language file: " + e.getMessage());
                }
            }
        }
    }
    
    public String getMessage(String key) {
        FileConfiguration config = languageConfigs.get(currentLanguage);
        if (config == null) {
            // Fallback to English if configured language doesn't exist
            config = languageConfigs.get("en");
        }
        
        if (config != null) {
            String message = config.getString(key);
            if (message != null) {
                return ChatColor.translateAlternateColorCodes('&', message);
            }
        }
        
        // Fallback to English if key doesn't exist in current language
        FileConfiguration enConfig = languageConfigs.get("en");
        if (enConfig != null) {
            String message = enConfig.getString(key, key); // Return key if not found
            return ChatColor.translateAlternateColorCodes('&', message);
        }
        
        return key; // Return the key if no language files are available
    }
    
    public String getMessage(String key, String... placeholders) {
        String message = getMessage(key);
        
        // Replace placeholders in the format %placeholder%
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        
        return message;
    }
    
    public void setCurrentLanguage(String languageCode) {
        this.currentLanguage = languageCode;
    }
}