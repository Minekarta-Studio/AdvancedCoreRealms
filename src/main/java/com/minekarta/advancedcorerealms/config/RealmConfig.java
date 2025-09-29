package com.minekarta.advancedcorerealms.config;

import org.bukkit.configuration.file.FileConfiguration;

public class RealmConfig {

    private final String templatesFolder;
    private final String serverRealmsFolder;
    private final String worldNameFormat;
    private final int defaultBorderSize;
    private final int sanitizeMaxLength;
    private final String sanitizeAllowedRegex;

    public RealmConfig(FileConfiguration config) {
        this.templatesFolder = config.getString("realms.templates-folder", "templates");
        this.serverRealmsFolder = config.getString("realms.server-realms-folder", "realms");
        this.worldNameFormat = config.getString("realms.world-name-format", "acr_{owner}_{name}_{ts}");
        this.defaultBorderSize = config.getInt("realms.default-border-size", 50);
        this.sanitizeMaxLength = config.getInt("realms.sanitize.max-length", 30);
        this.sanitizeAllowedRegex = config.getString("realms.sanitize.allowed-regex", "[a-z0-9_-]");
    }

    public String getTemplatesFolder() {
        return templatesFolder;
    }

    public String getServerRealmsFolder() {
        return serverRealmsFolder;
    }

    public String getWorldNameFormat() {
        return worldNameFormat;
    }

    public int getDefaultBorderSize() {
        return defaultBorderSize;
    }

    public int getSanitizeMaxLength() {
        return sanitizeMaxLength;
    }

    public String getSanitizeAllowedRegex() {
        return sanitizeAllowedRegex;
    }
}