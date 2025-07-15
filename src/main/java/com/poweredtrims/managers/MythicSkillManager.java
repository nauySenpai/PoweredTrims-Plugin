package com.poweredtrims.managers;

import com.poweredtrims.PoweredTrims;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class MythicSkillManager {
    
    private final PoweredTrims plugin;
    private boolean mythicMobsAvailable = false;
    
    public MythicSkillManager(PoweredTrims plugin) {
        this.plugin = plugin;
        checkMythicMobsAvailability();
    }
    
    private void checkMythicMobsAvailability() {
        try {
            Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            // Use reflection to check if MythicBukkit is available
            Class<?> mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            Object instance = mythicBukkitClass.getMethod("inst").invoke(null);
            mythicMobsAvailable = instance != null;

            if (mythicMobsAvailable) {
                plugin.getLogger().info("MythicMobs integration successfully initialized!");
            } else {
                plugin.getLogger().warning("MythicMobs plugin found but not properly initialized!");
            }
        } catch (ClassNotFoundException e) {
            mythicMobsAvailable = false;
            plugin.getLogger().info("MythicMobs not found - skill features will be disabled.");
        } catch (Exception e) {
            mythicMobsAvailable = false;
            plugin.getLogger().log(Level.WARNING, "Error initializing MythicMobs integration", e);
        }
    }
    
    public boolean executeSkill(Player player, String skillName) {
        if (!mythicMobsAvailable) {
            plugin.getLogger().fine("Cannot execute skill '" + skillName + "' - MythicMobs not available");
            return false;
        }
        
        if (skillName == null || skillName.trim().isEmpty()) {
            plugin.getLogger().fine("Cannot execute skill - skill name is null or empty");
            return false;
        }
        
        if (player == null || !player.isOnline()) {
            plugin.getLogger().fine("Cannot execute skill '" + skillName + "' - player is null or offline");
            return false;
        }
        
        try {
            // Use reflection to execute MythicMobs skill
            Class<?> mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            Object mythicInstance = mythicBukkitClass.getMethod("inst").invoke(null);
            Object skillManager = mythicBukkitClass.getMethod("getSkillManager").invoke(mythicInstance);

            Class<?> skillManagerClass = skillManager.getClass();
            Object skillOptional = skillManagerClass.getMethod("getSkill", String.class).invoke(skillManager, skillName);

            // Check if skill exists using Optional.isPresent()
            Class<?> optionalClass = skillOptional.getClass();
            boolean isPresent = (Boolean) optionalClass.getMethod("isPresent").invoke(skillOptional);

            if (!isPresent) {
                plugin.getLogger().warning("Skill '" + skillName + "' not found in MythicMobs!");
                return false;
            }

            // Get the skill from Optional
            Object skill = optionalClass.getMethod("get").invoke(skillOptional);

            // Execute skill using reflection - simplified approach
            // This is a basic implementation that may need adjustment based on MythicMobs version
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                "mythicmobs skills cast " + skillName + " " + player.getName());

            plugin.getLogger().fine("Successfully executed skill '" + skillName + "' for player " + player.getName());
            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING,
                "Error executing skill '" + skillName + "' for player " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    public boolean executeSkillWithDelay(Player player, String skillName, long delayTicks) {
        if (!mythicMobsAvailable) {
            return false;
        }
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            executeSkill(player, skillName);
        }, delayTicks);
        
        return true;
    }
    
    public boolean isSkillAvailable(String skillName) {
        if (!mythicMobsAvailable) {
            return false;
        }

        try {
            Class<?> mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            Object mythicInstance = mythicBukkitClass.getMethod("inst").invoke(null);
            Object skillManager = mythicBukkitClass.getMethod("getSkillManager").invoke(mythicInstance);

            Class<?> skillManagerClass = skillManager.getClass();
            Object skillOptional = skillManagerClass.getMethod("getSkill", String.class).invoke(skillManager, skillName);

            Class<?> optionalClass = skillOptional.getClass();
            return (Boolean) optionalClass.getMethod("isPresent").invoke(skillOptional);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error checking skill availability: " + skillName, e);
            return false;
        }
    }
    
    public boolean isMythicMobsAvailable() {
        return mythicMobsAvailable;
    }
    
    public void reload() {
        checkMythicMobsAvailability();
    }
    
    // Utility method to validate skill names from config
    public void validateConfiguredSkills() {
        if (!mythicMobsAvailable) {
            return;
        }
        
        plugin.getConfigManager().getAllTrimConfigs().forEach((trimName, trimConfig) -> {
            if (trimConfig.hasOnEquipSkill()) {
                String skill = trimConfig.getOnEquipSkill();
                if (!isSkillAvailable(skill)) {
                    plugin.getLogger().warning("Trim '" + trimName + "' references unknown onEquip skill: " + skill);
                }
            }
            
            if (trimConfig.hasOnUnequipSkill()) {
                String skill = trimConfig.getOnUnequipSkill();
                if (!isSkillAvailable(skill)) {
                    plugin.getLogger().warning("Trim '" + trimName + "' references unknown onUnequip skill: " + skill);
                }
            }
            
            if (trimConfig.hasSetBonusSkill()) {
                String skill = trimConfig.getSetBonusSkill();
                if (!isSkillAvailable(skill)) {
                    plugin.getLogger().warning("Trim '" + trimName + "' references unknown setBonus skill: " + skill);
                }
            }
        });
    }
}
