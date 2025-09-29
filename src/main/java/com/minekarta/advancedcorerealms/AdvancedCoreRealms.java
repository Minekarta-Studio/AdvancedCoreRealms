package com.minekarta.advancedcorerealms;

import com.minekarta.advancedcorerealms.api.AdvancedCorePlayer;
import com.minekarta.advancedcorerealms.api.AdvancedCorePlayerImpl;
import com.minekarta.advancedcorerealms.config.RealmConfig;
import com.minekarta.advancedcorerealms.realm.RealmCreator;
import com.minekarta.advancedcorerealms.economy.EconomyService;
import com.minekarta.advancedcorerealms.economy.NoopEconomyService;
import com.minekarta.advancedcorerealms.economy.VaultEconomyService;
import com.minekarta.advancedcorerealms.realm.RealmInventoryService;
import com.minekarta.advancedcorerealms.storage.DatabaseManager;
import com.minekarta.advancedcorerealms.storage.InventoryStorage;
import com.minekarta.advancedcorerealms.storage.YamlInventoryStorage;
import com.minekarta.advancedcorerealms.transactions.TransactionLogger;
import com.minekarta.advancedcorerealms.upgrades.UpgradeManager;
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
    private RealmConfig realmConfig;
    private RealmCreator realmCreator;
    private InventoryStorage inventoryStorage;
    private RealmInventoryService realmInventoryService;
    private EconomyService economyService;
    private TransactionLogger transactionLogger;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize Database Manager first
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initialize();

        // Initialize managers
        this.languageManager = new LanguageManager(this);
        this.worldManager = new WorldManager(this);
        this.inviteManager = new InviteManager(this);
        this.menuManager = new MenuManager(this);
        this.guiManager = new GUIManager(this);
        this.upgradeManager = new UpgradeManager(this);
        this.playerStateManager = new PlayerStateManager(this);
        this.realmManager = new RealmManager(this);

        // Load configuration
        saveDefaultConfig();
        this.realmConfig = new RealmConfig(getConfig());

        // Create default template directory if it doesn't exist
        createDefaultTemplateDirectory();

        this.realmCreator = new RealmCreator(this);

        // Initialize storage and services
        this.inventoryStorage = new YamlInventoryStorage(this);
        this.realmInventoryService = new RealmInventoryService(this, this.inventoryStorage);
        this.transactionLogger = new TransactionLogger(this);

        // Register PlaceholderAPI if it's available
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AdvancedCoreRealmsPlaceholder(this).register();
        }

        this.languageManager.loadLanguage();

        // Load upgrades and initialize economy
        this.upgradeManager.loadUpgrades();
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
        File templatesFolder = new File(getDataFolder(), realmConfig.getTemplatesFolder());
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

    @Override
    public void onDisable() {
        // Close database connection pool
        if (databaseManager != null) {
            databaseManager.close();
        }

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

    public RealmConfig getRealmConfig() {
        return realmConfig;
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

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
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