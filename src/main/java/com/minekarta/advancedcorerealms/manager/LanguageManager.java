package com.minekarta.advancedcorerealms.manager;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    
    private final AdvancedCoreRealms plugin;
    private final Map<String, FileConfiguration> languageConfigs;
    private String currentLanguage;
    private final MiniMessage miniMessage;
    
    public LanguageManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.languageConfigs = new HashMap<>();
        this.miniMessage = MiniMessage.miniMessage();
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
                        config.set("prefix", "<dark_gray>[<aqua>Realms<dark_gray>] <reset>");
                        config.set("command.help", "<green>Showing help for AdvancedCoreRealms...");
                        config.set("command.reloaded", "<green>Configuration reloaded successfully.");
                        config.set("world.created", "<green>Successfully created your new realm named <yellow>%world%<green>.");
                        config.set("world.deleted", "<red>Successfully deleted your realm <yellow>%world%<red>.");
                        config.set("world.teleport", "<gray>Teleporting you to <yellow>%world%<gray>...");
                        config.set("error.no-permission", "<red>You do not have permission to use this command.");
                        config.set("error.world-exists", "<red>A realm with that name already exists.");
                        config.set("error.not-owner", "<red>You are not the owner of this realm.");
                    }
                    // Spanish defaults
                    else if (lang.equals("es")) {
                        config.set("prefix", "<dark_gray>[<aqua>Reinos<dark_gray>] <reset>");
                        config.set("command.help", "<green>Mostrando ayuda para AdvancedCoreRealms...");
                        config.set("command.reloaded", "<green>Configuración recargada exitosamente.");
                        config.set("world.created", "<green>Se creó exitosamente tu nuevo reino llamado <yellow>%world%<green>.");
                        config.set("world.deleted", "<red>Se eliminó exitosamente tu reino <yellow>%world%<red>.");
                        config.set("world.teleport", "<gray>Teletransportándote a <yellow>%world%<gray>...");
                        config.set("error.no-permission", "<red>No tienes permiso para usar este comando.");
                        config.set("error.world-exists", "<red>Ya existe un reino con ese nombre.");
                        config.set("error.not-owner", "<red>No eres el propietario de este reino.");
                    }
                    // Indonesian defaults
                    else if (lang.equals("id")) {
                        config.set("prefix", "<dark_gray>[<aqua>Realms<dark_gray>] <reset>");
                        config.set("command.help", "<green>Menampilkan bantuan untuk AdvancedCoreRealms...");
                        config.set("command.reloaded", "<green>Konfigurasi berhasil dimuat ulang.");
                        config.set("world.created", "<green>Berhasil membuat realm baru bernama <yellow>%world%<green>.");
                        config.set("world.deleted", "<red>Berhasil menghapus realm <yellow>%world%<red>.");
                        config.set("world.teleport", "<gray>Teleportasi ke <yellow>%world%<gray>...");
                        config.set("error.no-permission", "<red>Anda tidak memiliki izin untuk menggunakan perintah ini.");
                        config.set("error.world-exists", "<red>Realm dengan nama tersebut sudah ada.");
                        config.set("error.not-owner", "<red>Anda bukan pemilik dari realm ini.");
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
                return message; // Return raw message, let ColorUtils handle formatting
            }
        }
        
        // Fallback to English if key doesn't exist in current language
        FileConfiguration enConfig = languageConfigs.get("en");
        if (enConfig != null) {
            String message = enConfig.getString(key, key); // Return key if not found
            return message; // Return raw message, let ColorUtils handle formatting
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
    
    public Component getMessageAsComponent(String key) {
        String message = getMessage(key);
        return miniMessage.deserialize(message);
    }
    
    public Component getMessageAsComponent(String key, String... placeholders) {
        String message = getMessage(key);
        
        // Replace placeholders in the format %placeholder%
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        
        return miniMessage.deserialize(message);
    }
    
    public void setCurrentLanguage(String languageCode) {
        this.currentLanguage = languageCode;
    }
}