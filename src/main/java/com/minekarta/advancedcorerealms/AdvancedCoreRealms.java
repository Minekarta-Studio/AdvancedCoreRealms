package com.minekarta.advancedcorerealms;

import com.minekarta.advancedcorerealms.api.AdvancedCorePlayer;
import com.minekarta.advancedcorerealms.api.AdvancedCorePlayerImpl;
import com.minekarta.advancedcorerealms.upgrades.UpgradeManager;
import com.minekarta.advancedcorerealms.commands.RealmsCommand;
import com.minekarta.advancedcorerealms.data.PlayerDataManager;
import com.minekarta.advancedcorerealms.data.WorldDataManager;
import com.minekarta.advancedcorerealms.gui.GUIManager;
import com.minekarta.advancedcorerealms.listeners.InventoryClickListener;
import com.minekarta.advancedcorerealms.listeners.InventoryListener;
import com.minekarta.advancedcorerealms.listeners.PlayerConnectionListener;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import com.minekarta.advancedcorerealms.placeholder.AdvancedCoreRealmsPlaceholder;
import com.minekarta.advancedcorerealms.listeners.PlayerWorldListener;
import com.minekarta.advancedcorerealms.manager.InviteManager;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.PlayerStateManager;
import com.minekarta.advancedcorerealms.manager.world.WorldManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvancedCoreRealms extends JavaPlugin {
    
    private static AdvancedCoreRealms instance;
    private WorldManager worldManager;
    private InviteManager inviteManager;
    private LanguageManager languageManager;
    private WorldDataManager worldDataManager;
    private PlayerDataManager playerDataManager;
    private MenuManager menuManager;
    private GUIManager guiManager;
    private UpgradeManager upgradeManager;
    private PlayerStateManager playerStateManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        this.languageManager = new LanguageManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.worldDataManager = new WorldDataManager(this);
        this.worldManager = new WorldManager(this);
        this.inviteManager = new InviteManager(this);
        this.menuManager = new MenuManager(this);
        this.guiManager = new GUIManager(this);
        this.upgradeManager = new UpgradeManager(this);
        this.playerStateManager = new PlayerStateManager(this);
        
        // Register PlaceholderAPI if it's available
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AdvancedCoreRealmsPlaceholder(this).register();
        }
        
        // Load configuration
        saveDefaultConfig();
        this.languageManager.loadLanguage();
        
        // Load upgrades and initialize economy
        this.upgradeManager.loadUpgrades();
        this.upgradeManager.initializeEconomy();
        
        // Register commands
        getCommand("realms").setExecutor(new RealmsCommand(this));
        getCommand("realms").setTabCompleter(new RealmsCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        
        // Initialize data managers
        this.worldDataManager.loadData();
        
        getLogger().info("AdvancedCoreRealms has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Save data on shutdown
        if (worldDataManager != null) {
            worldDataManager.saveData();
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
    
    public WorldDataManager getWorldDataManager() {
        return worldDataManager;
    }
    
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
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