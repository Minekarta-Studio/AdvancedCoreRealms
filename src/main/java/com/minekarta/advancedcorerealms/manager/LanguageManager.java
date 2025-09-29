package com.minekarta.advancedcorerealms.manager;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LanguageManager {

    private final AdvancedCoreRealms plugin;
    private FileConfiguration languageConfig;
    private final MiniMessage miniMessage;

    public LanguageManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public void loadLanguage() {
        // Define the languages to be exported
        String[] languages = {"en", "es", "id"};
        File languagesDir = new File(plugin.getDataFolder(), "languages");
        if (!languagesDir.exists()) {
            languagesDir.mkdirs();
        }

        // Save default language files if they don't exist
        for (String lang : languages) {
            File langFile = new File(languagesDir, lang + ".yml");
            if (!langFile.exists()) {
                plugin.saveResource("languages/" + lang + ".yml", false);
            }
        }

        // Load the configured language
        String configuredLanguage = plugin.getConfig().getString("language", "en");
        File languageFile = new File(languagesDir, configuredLanguage + ".yml");

        // Fallback to English if the configured language file doesn't exist
        if (!languageFile.exists()) {
            plugin.getLogger().warning("Language file '" + configuredLanguage + ".yml' not found. Falling back to 'en.yml'.");
            languageFile = new File(languagesDir, "en.yml");
        }

        this.languageConfig = YamlConfiguration.loadConfiguration(languageFile);

        // Load default values from the JAR's en.yml as a fallback
        try (InputStream defaultConfigStream = plugin.getResource("languages/en.yml")) {
            if (defaultConfigStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8));
                this.languageConfig.setDefaults(defaultConfig);
            }
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Could not load default English language file from JAR.", e);
        }

        plugin.getLogger().info("Loaded language: " + configuredLanguage);
    }

    public String getMessage(String key) {
        return languageConfig.getString(key, "&cMissing language key: " + key);
    }

    public String getMessage(String key, String... placeholders) {
        String message = getMessage(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        return message;
    }
    
    public void sendMessage(CommandSender sender, String key, String... placeholders) {
        String prefix = getMessage("prefix");
        String message = getMessage(key);

        List<TagResolver> tagResolvers = new ArrayList<>();
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                // Remove the placeholder markers (e.g., '%' or '<' and '>') for MiniMessage
                String placeholderKey = placeholders[i].replaceAll("[<%>%]", "");
                tagResolvers.add(Placeholder.parsed(placeholderKey, placeholders[i+1]));
            }
        }

        Component parsedMessage = miniMessage.deserialize(message, TagResolver.resolver(tagResolvers));

        if (key.equals("prefix") || message.isEmpty()) {
            sender.sendMessage(parsedMessage);
        } else {
            Component prefixComponent = miniMessage.deserialize(prefix);
            sender.sendMessage(prefixComponent.append(Component.space()).append(parsedMessage));
        }
    }

    public void sendMessage(Player player, String key, String... placeholders) {
        sendMessage((CommandSender) player, key, placeholders);
    }
}