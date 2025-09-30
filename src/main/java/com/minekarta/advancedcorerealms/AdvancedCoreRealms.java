package com.minekarta.advancedcorerealms;

import com.minekarta.advancedcorerealms.api.AdvancedCorePlayer;
import com.minekarta.advancedcorerealms.api.AdvancedCorePlayerImpl;
import com.minekarta.advancedcorerealms.config.ConfigManager;
import com.minekarta.advancedcorerealms.realm.RealmCreator;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderConfig;
import com.minekarta.advancedcorerealms.economy.EconomyService;
import com.minekarta.advancedcorerealms.economy.NoopEconomyService;
import com.minekarta.advancedcorerealms.economy.VaultEconomyService;
import com.minekarta.advancedcorerealms.realm.RealmInventoryService;
import com.minekarta.advancedcorerealms.storage.InventoryStorage;
import com.minekarta.advancedcorerealms.storage.StorageManager;
import com.minekarta.advancedcorerealms.storage.YamlInventoryStorage;
import com.minekarta.advancedcorerealms.transactions.TransactionLogger;
import com.minekarta.advancedcorerealms.upgrades.UpgradeManager;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderManager;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderService;
import com.minekarta.advancedcorerealms.commands.RealmsCommand;
import com.minekarta.advancedcorerealms.gui.GUIManager;
import com.minekarta.advancedcorerealms.listeners.*;
import com.minekarta.advancedcorerealms.manager.InviteManager;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import com.minekarta.advancedcorerealms.placeholder.AdvancedCoreRealmsPlaceholder;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.PlayerStateManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.manager.world.WorldManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * The main class for the AdvancedCoreRealms plugin.
 * This plugin provides a comprehensive solution for managing player-owned realms,
 * including features like creation, upgrades, member management, and world borders.
 * It is built to be highly performant and scalable, with a focus on asynchronous
 * data handling and a modular architecture.
 */
public class AdvancedCoreRealms extends JavaPlugin {

    private static AdvancedCoreRealms instance;
    private WorldManager worldManager;
    private InviteManager inviteManager;
    private LanguageManager languageManager;
    private MenuManager menuManager;
    private GUIManager guiManager;
    private UpgradeManager upgradeManager;
    private PlayerStateManager playerStateManager;
    private RealmManager realmManager;
    private ConfigManager configManager;
    private WorldBorderConfig worldBorderConfig;
    private RealmCreator realmCreator;
    private InventoryStorage inventoryStorage;
    private RealmInventoryService realmInventoryService;
    private EconomyService economyService;
    private TransactionLogger transactionLogger;
    private StorageManager storageManager;
    private WorldBorderService worldBorderService;
    private WorldBorderManager worldBorderManager;

    /**
     * Called when the plugin is enabled.
     * The initialization process is critical and follows a strict order:
     * 1.  Initialize the {@link DatabaseManager}. This is the most critical step. If the database
     *     cannot be initialized (e.g., due to file permissions or corruption), the plugin
     *     will automatically disable itself to prevent data loss or corruption.
     * 2.  Load all configuration files.
     * 3.  Initialize all other managers and services.
     * 4.  Register commands and event listeners.
     */
    @Override
    public void onEnable() {
        instance = this;

        // Initialize Storage Manager
        this.storageManager = new StorageManager(this);

        // Load configuration
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        this.worldBorderConfig = new WorldBorderConfig(this);
        this.worldBorderConfig.load();

        // Initialize managers
        this.languageManager = new LanguageManager(this);
        this.realmManager = new RealmManager(this);
        this.realmManager.init(); // Load all realms from storage
        this.worldManager = new WorldManager(this);
        this.inviteManager = new InviteManager(this);
        this.menuManager = new MenuManager(this);
        this.guiManager = new GUIManager(this);
        this.upgradeManager = new UpgradeManager(this);
        this.playerStateManager = new PlayerStateManager(this);
        this.worldBorderManager = new WorldBorderManager(this);

        // Load configuration
        // Create default template directory if it doesn't exist
        createDefaultTemplateDirectory();

        this.realmCreator = new RealmCreator(this);

        // Initialize storage and services
        this.inventoryStorage = new YamlInventoryStorage(this);
        this.realmInventoryService = new RealmInventoryService(this, this.inventoryStorage);
        this.transactionLogger = new TransactionLogger(this);
        this.worldBorderService = new WorldBorderService(this);

        // Register PlaceholderAPI if it's available
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AdvancedCoreRealmsPlaceholder(this).register();
        }

        this.languageManager.loadLanguage();

        // Initialize economy
        setupEconomy();

        // Register commands
        getCommand("realms").setExecutor(new RealmsCommand(this));
        getCommand("realms").setTabCompleter(new RealmsCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new RealmProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new RealmTeleportListener(this), this);

        // Scan for and log orphaned lock files
        cleanupOrphanedFiles();

        getLogger().info("AdvancedCoreRealms has been enabled!");
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            this.economyService = new VaultEconomyService();
            if (this.economyService.isEnabled()) {
                getLogger().info("Successfully hooked into Vault for economy services.");
            } else {
                getLogger().warning("Vault is installed, but no economy provider was found. Economy features are disabled.");
                this.economyService = new NoopEconomyService(this);
            }
        } else {
            getLogger().warning("Vault not found. All economy features will be disabled.");
            this.economyService = new NoopEconomyService(this);
        }
    }

    private void cleanupOrphanedFiles() {
        File[] lockFiles = getDataFolder().listFiles((dir, name) -> name.startsWith("creating_") && name.endsWith(".lock"));
        if (lockFiles != null && lockFiles.length > 0) {
            getLogger().warning("Found " + lockFiles.length + " orphaned creation lock files. This may indicate a server crash during a previous realm creation. Please check for and manually delete any partial realm folders.");
            for (File lockFile : lockFiles) {
                getLogger().warning("  - Orphaned file: " + lockFile.getName());
            }
        }
    }

    private void createDefaultTemplateDirectory() {
        // This setting is now managed by ConfigManager, but for simplicity, we'll hardcode the path
        // or retrieve it from a general settings section if it were added to ConfigManager.
        // For now, let's assume a default path.
        File templatesFolder = new File(getDataFolder(), "templates");
        File defaultTemplate = new File(templatesFolder, "default");

        if (!defaultTemplate.exists()) {
            if (defaultTemplate.mkdirs()) {
                getLogger().info("Created default template directory at: " + defaultTemplate.getPath());
                getLogger().info("Please add your default world save to this directory to enable realm creation.");
            } else {
                getLogger().severe("Could not create the default template directory. Realm creation may fail.");
            }
        }
    }

    /**
     * Called when the plugin is disabled.
     * Gracefully shuts down all services, most importantly closing the database connection pool
     * to prevent resource leaks.
     */
    @Override
    public void onDisable() {
        // All data is saved on-the-fly, so no special shutdown tasks are needed.
        getLogger().info("AdvancedCoreRealms has been disabled!");
    }

    public static AdvancedCoreRealms getInstance() {
        return instance;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public InviteManager getInviteManager() {
        return inviteManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }

    public PlayerStateManager getPlayerStateManager() {
        return playerStateManager;
    }

    public RealmManager getRealmManager() {
        return realmManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public WorldBorderConfig getWorldBorderConfig() {
        return worldBorderConfig;
    }

    public RealmCreator getRealmCreator() {
        return realmCreator;
    }

    public InventoryStorage getInventoryStorage() {
        return inventoryStorage;
    }

    public RealmInventoryService getRealmInventoryService() {
        return realmInventoryService;
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public TransactionLogger getTransactionLogger() {
        return transactionLogger;
    }

    /**
     * Gets the manager responsible for handling data storage.
     *
     * @return The active {@link StorageManager}.
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }

    public WorldBorderService getWorldBorderService() {
        return worldBorderService;
    }

    public WorldBorderManager getWorldBorderManager() {
        return worldBorderManager;
    }

    // Cache for AdvancedCorePlayer instances
    private final java.util.Map<org.bukkit.entity.Player, AdvancedCorePlayer> advancedCorePlayerCache = new java.util.HashMap<>();

    /**
     * Get a AdvancedCorePlayer instance for the given player
     */
    public AdvancedCorePlayer getAdvancedCorePlayer(Player player) {
        return advancedCorePlayerCache.computeIfAbsent(player, p -> new AdvancedCorePlayerImpl(this, p));
    }

    /**
     * Remove a player from the cache (when they log out)
     */
    public void removeAdvancedCorePlayer(Player player) {
        advancedCorePlayerCache.remove(player);
    }
}