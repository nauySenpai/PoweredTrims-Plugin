package com.poweredtrims;

import com.poweredtrims.commands.PoweredTrimsCommand;
import com.poweredtrims.config.ConfigManager;
import com.poweredtrims.listeners.ArmorEquipListener;
import com.poweredtrims.listeners.ItemUpdateListener;
import com.poweredtrims.managers.LoreManager;
import com.poweredtrims.managers.MythicSkillManager;
import com.poweredtrims.managers.TrimManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class PoweredTrims extends JavaPlugin {
    
    private static PoweredTrims instance;
    private static Logger logger;
    
    private ConfigManager configManager;
    private TrimManager trimManager;
    private LoreManager loreManager;
    private MythicSkillManager mythicSkillManager;
    
    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        
        logger.info("PoweredTrims is starting...");
        
        // Initialize managers
        initializeManagers();
        
        // Register listeners
        registerListeners();
        
        // Register commands
        registerCommands();
        
        logger.info("PoweredTrims has been enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        logger.info("PoweredTrims is shutting down...");
        
        // Clean up resources
        if (trimManager != null) {
            trimManager.cleanup();
        }
        
        logger.info("PoweredTrims has been disabled.");
    }
    
    private void initializeManagers() {
        // Initialize config manager first
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        // Initialize other managers
        trimManager = new TrimManager(this);
        loreManager = new LoreManager(this);
        
        // Initialize MythicMobs integration if available
        if (getServer().getPluginManager().getPlugin("MythicMobs") != null) {
            mythicSkillManager = new MythicSkillManager(this);
            logger.info("MythicMobs integration enabled!");
        } else {
            logger.info("MythicMobs not found - skill features disabled.");
        }
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ArmorEquipListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemUpdateListener(this), this);
    }
    
    private void registerCommands() {
        PoweredTrimsCommand commandExecutor = new PoweredTrimsCommand(this);
        getCommand("poweredtrims").setExecutor(commandExecutor);
        getCommand("poweredtrims").setTabCompleter(commandExecutor);
    }
    
    public void reload() {
        logger.info("Reloading PoweredTrims...");
        
        // Reload configs
        configManager.loadConfigs();
        
        // Reload managers
        trimManager.reload();
        loreManager.reload();
        
        if (mythicSkillManager != null) {
            mythicSkillManager.reload();
        }
        
        logger.info("PoweredTrims reloaded successfully!");
    }
    
    // Getters
    public static PoweredTrims getInstance() {
        return instance;
    }
    
    public static Logger getPluginLogger() {
        return logger;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public TrimManager getTrimManager() {
        return trimManager;
    }
    
    public LoreManager getLoreManager() {
        return loreManager;
    }
    
    public MythicSkillManager getMythicSkillManager() {
        return mythicSkillManager;
    }
    
    public boolean isMythicMobsEnabled() {
        return mythicSkillManager != null;
    }
}
