package com.minekarta.advancedcorerealms;

import com.minekarta.advancedcorerealms.commands.RealmsCommand;
import com.minekarta.advancedcorerealms.data.PlayerDataManager;
import com.minekarta.advancedcorerealms.data.WorldDataManager;
import com.minekarta.advancedcorerealms.listeners.PlayerConnectionListener;
import com.minekarta.advancedcorerealms.listeners.PlayerWorldListener;
import com.minekarta.advancedcorerealms.manager.InviteManager;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvancedCoreRealms extends JavaPlugin {
    
    private static AdvancedCoreRealms instance;
    private WorldManager worldManager;
    private InviteManager inviteManager;
    private LanguageManager languageManager;
    private WorldDataManager worldDataManager;
    private PlayerDataManager playerDataManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        this.languageManager = new LanguageManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.worldDataManager = new WorldDataManager(this);
        this.worldManager = new WorldManager(this);
        this.inviteManager = new InviteManager(this);
        
        // Load configuration
        saveDefaultConfig();
        this.languageManager.loadLanguage();
        
        // Register commands
        getCommand("realms").setExecutor(new RealmsCommand(this));
        getCommand("realms").setTabCompleter(new RealmsCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldListener(this), this);
        
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
}