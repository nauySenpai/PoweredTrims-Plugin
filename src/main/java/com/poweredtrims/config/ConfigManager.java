package com.poweredtrims.config;

import com.poweredtrims.PoweredTrims;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {
    
    private final PoweredTrims plugin;
    private FileConfiguration config;
    private FileConfiguration loreFormatConfig;
    
    private final Map<String, TrimConfig> trimConfigs = new HashMap<>();
    private final Map<String, List<String>> loreTemplates = new HashMap<>();
    
    public ConfigManager(PoweredTrims plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfigs() {
        loadMainConfig();
        loadLoreFormatConfig();
        extractTrimFiles();
        loadTrimConfigs();
        loadLoreTemplates();
    }
    
    private void loadMainConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // Create default config if it doesn't exist
        if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
            createDefaultConfig();
        }
    }
    
    private void loadLoreFormatConfig() {
        File loreFormatFile = new File(plugin.getDataFolder(), "lore-format.yml");
        
        if (!loreFormatFile.exists()) {
            plugin.saveResource("lore-format.yml", false);
        }
        
        loreFormatConfig = YamlConfiguration.loadConfiguration(loreFormatFile);
        
        // Load defaults from jar
        InputStream defConfigStream = plugin.getResource("lore-format.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            loreFormatConfig.setDefaults(defConfig);
        }
    }
    
    private void loadTrimConfigs() {
        trimConfigs.clear();

        File trimsFolder = new File(plugin.getDataFolder(), "trims");
        if (!trimsFolder.exists()) {
            plugin.getLogger().warning("Trims folder not found! Creating default structure...");
            return;
        }

        // Load trim configs from individual files
        loadTrimConfigsFromFiles(trimsFolder);

        plugin.getLogger().info("Loaded " + trimConfigs.size() + " trim configurations.");
    }

    private void loadTrimConfigsFromFiles(File trimsFolder) {
        File[] patternFolders = trimsFolder.listFiles(File::isDirectory);
        if (patternFolders == null) return;

        for (File patternFolder : patternFolders) {
            String patternName = patternFolder.getName();

            File[] materialFiles = patternFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (materialFiles == null) continue;

            for (File materialFile : materialFiles) {
                String materialName = materialFile.getName().replace(".yml", "");
                String trimKey = patternName + "_" + materialName;

                try {
                    FileConfiguration trimConfig = YamlConfiguration.loadConfiguration(materialFile);
                    TrimConfig config = loadTrimConfigFromYaml(trimKey, trimConfig);
                    if (config != null) {
                        trimConfigs.put(trimKey, config);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load trim config: " + materialFile.getName() + " - " + e.getMessage());
                }
            }
        }
    }

    private TrimConfig loadTrimConfigFromYaml(String trimKey, FileConfiguration yaml) {
        TrimConfig trimConfig = new TrimConfig(trimKey);

        // Load basic settings
        trimConfig.setEnabled(yaml.getBoolean("enabled", true));

        // Load attributes
        ConfigurationSection attributesSection = yaml.getConfigurationSection("attributes");
        if (attributesSection != null) {
            for (String attrName : attributesSection.getKeys(false)) {
                double value = attributesSection.getDouble(attrName);
                trimConfig.addAttribute(attrName, value);
            }
        }

        // Load effects
        List<Map<?, ?>> effectsList = yaml.getMapList("effects");
        for (Map<?, ?> effectMap : effectsList) {
            String type = (String) effectMap.get("type");

            int amplifier = 0;
            int duration = 999999; // Default to permanent duration

            if (effectMap.containsKey("amplifier")) {
                Object amplifierObj = effectMap.get("amplifier");
                if (amplifierObj instanceof Integer) {
                    amplifier = (Integer) amplifierObj;
                }
            }

            trimConfig.addEffect(type, amplifier, duration);
        }

        // Load mythic skills
        ConfigurationSection skillsSection = yaml.getConfigurationSection("mythicskills");
        if (skillsSection != null) {
            String onEquip = skillsSection.getString("onEquip");
            String onUnequip = skillsSection.getString("onUnequip");
            String setBonus = skillsSection.getString("setBonus");

            trimConfig.setSkills(onEquip, onUnequip, setBonus);
        }

        // Load set bonus
        ConfigurationSection setBonusSection = yaml.getConfigurationSection("set_bonus");
        if (setBonusSection != null && setBonusSection.getBoolean("enabled", false)) {
            // Set bonus loading logic can be added here
            // For now, we'll use the existing setBonus skill approach
        }

        // Load lore settings
        ConfigurationSection loreSection = yaml.getConfigurationSection("lore");
        if (loreSection != null) {
            boolean loreEnabled = loreSection.getBoolean("enabled", true);
            String template = loreSection.getString("template", "default");

            trimConfig.setLoreEnabled(loreEnabled);
            trimConfig.setLoreTemplate(template);
        }

        return trimConfig;
    }

    private void extractTrimFiles() {
        File trimsFolder = new File(plugin.getDataFolder(), "trims");

        // Create trims folder if it doesn't exist
        if (!trimsFolder.exists()) {
            trimsFolder.mkdirs();
        }

        // Define all patterns and materials
        String[] patterns = {"bolt", "coast", "dune", "eye", "flow", "host", "raiser", "rib",
                           "sentry", "shaper", "silence", "snout", "spire", "tide", "vex",
                           "ward", "wayfinder", "wild"};
        String[] materials = {"amethyst_shard", "copper", "diamond", "emerald", "gold",
                            "iron", "lapis_lazuli", "nether_quartz", "netherite", "redstone",
                            "resin_brick"};

        int extractedCount = 0;

        for (String pattern : patterns) {
            File patternFolder = new File(trimsFolder, pattern);
            if (!patternFolder.exists()) {
                patternFolder.mkdirs();
            }

            for (String material : materials) {
                String resourcePath = "trims/" + pattern + "/" + material + ".yml";
                File targetFile = new File(patternFolder, material + ".yml");

                // Only extract if file doesn't exist
                if (!targetFile.exists()) {
                    try {
                        plugin.saveResource(resourcePath, false);
                        extractedCount++;
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to extract " + resourcePath + ": " + e.getMessage());
                    }
                }
            }
        }

        if (extractedCount > 0) {
            plugin.getLogger().info("Extracted " + extractedCount + " trim configuration files.");
        }
    }

    private void loadLoreTemplates() {
        loreTemplates.clear();
        
        for (String templateName : loreFormatConfig.getKeys(false)) {
            List<String> template = loreFormatConfig.getStringList(templateName);
            loreTemplates.put(templateName, template);
        }
        
        plugin.getLogger().info("Loaded " + loreTemplates.size() + " lore templates.");
    }
    
    private void createDefaultConfig() {
        try {
            plugin.saveResource("config.yml", true);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create default config.yml", e);
        }
    }
    
    // Getters
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getLoreFormatConfig() {
        return loreFormatConfig;
    }
    
    public TrimConfig getTrimConfig(String trimName) {
        return trimConfigs.get(trimName.toLowerCase());
    }
    
    public Map<String, TrimConfig> getAllTrimConfigs() {
        return new HashMap<>(trimConfigs);
    }
    
    public List<String> getLoreTemplate(String templateName) {
        return loreTemplates.get(templateName);
    }
    
    public Map<String, List<String>> getAllLoreTemplates() {
        return new HashMap<>(loreTemplates);
    }
    
    // Settings getters
    public boolean isLoreEnabled() {
        return config.getBoolean("settings.lore.enabled", true);
    }

    public boolean useLoreFormatFile() {
        return config.getBoolean("settings.lore.use-lore-format-file", true);
    }
}
