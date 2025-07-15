package com.poweredtrims;

import com.poweredtrims.config.TrimConfig;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * PoweredTrims API for external plugins
 * 
 * This class provides static methods to interact with PoweredTrims functionality
 * from other plugins.
 */
public class PoweredTrimsAPI {
    
    /**
     * Get the extra attribute value a player receives from their equipped trims
     * 
     * @param player The player to check
     * @param attr The attribute to check
     * @return The total extra attribute value from all equipped trims
     */
    public static double getExtraAttribute(Player player, Attribute attr) {
        if (player == null || attr == null) return 0.0;
        
        PoweredTrims plugin = PoweredTrims.getInstance();
        if (plugin == null) return 0.0;
        
        double total = 0.0;
        ItemStack[] armor = player.getInventory().getArmorContents();
        
        for (ItemStack item : armor) {
            if (item == null || !(item.getItemMeta() instanceof ArmorMeta)) continue;
            
            ArmorMeta meta = (ArmorMeta) item.getItemMeta();
            ArmorTrim trim = meta.getTrim();
            
            if (trim != null) {
                String patternName = trim.getPattern().getKey().getKey();
                String materialName = trim.getMaterial().getKey().getKey();
                String trimKey = patternName + "_" + materialName;
                TrimConfig trimConfig = plugin.getConfigManager().getTrimConfig(trimKey);
                
                if (trimConfig != null && trimConfig.isEnabled()) {
                    total += trimConfig.getAttributeValue(attr.name());
                }
            }
        }
        
        return total;
    }
    
    /**
     * Get all potion effects that should be applied from a specific armor piece's trim
     * 
     * @param armorPiece The armor piece to check
     * @return List of potion effects from the trim, empty list if no trim or no effects
     */
    public static List<PotionEffect> getTrimEffects(ItemStack armorPiece) {
        List<PotionEffect> effects = new ArrayList<>();
        
        if (armorPiece == null || !(armorPiece.getItemMeta() instanceof ArmorMeta)) {
            return effects;
        }
        
        PoweredTrims plugin = PoweredTrims.getInstance();
        if (plugin == null) return effects;
        
        ArmorMeta meta = (ArmorMeta) armorPiece.getItemMeta();
        ArmorTrim trim = meta.getTrim();
        
        if (trim != null) {
            String patternName = trim.getPattern().getKey().getKey();
            String materialName = trim.getMaterial().getKey().getKey();
            String trimKey = patternName + "_" + materialName;
            TrimConfig trimConfig = plugin.getConfigManager().getTrimConfig(trimKey);
            
            if (trimConfig != null && trimConfig.isEnabled()) {
                Map<String, TrimConfig.EffectData> trimEffects = trimConfig.getEffects();
                
                for (TrimConfig.EffectData effectData : trimEffects.values()) {
                    try {
                        PotionEffectType effectType = PotionEffectType.getByName(effectData.getType());
                        if (effectType != null) {
                            PotionEffect effect = new PotionEffect(
                                effectType,
                                effectData.getDuration(),
                                effectData.getAmplifier(),
                                false,
                                false,
                                true
                            );
                            effects.add(effect);
                        }
                    } catch (Exception e) {
                        // Ignore invalid effects
                    }
                }
            }
        }
        
        return effects;
    }
    
    /**
     * Trigger a MythicMobs skill for a player
     * 
     * @param player The player to execute the skill for
     * @param skillName The name of the skill to execute
     * @return true if the skill was executed successfully, false otherwise
     */
    public static boolean triggerSkill(Player player, String skillName) {
        if (player == null || skillName == null || skillName.trim().isEmpty()) {
            return false;
        }
        
        PoweredTrims plugin = PoweredTrims.getInstance();
        if (plugin == null || !plugin.isMythicMobsEnabled()) {
            return false;
        }
        
        return plugin.getMythicSkillManager().executeSkill(player, skillName);
    }
    
    /**
     * Get the trim name from an armor piece
     * 
     * @param armorPiece The armor piece to check
     * @return The trim name, or null if no trim
     */
    public static String getTrimName(ItemStack armorPiece) {
        if (armorPiece == null || !(armorPiece.getItemMeta() instanceof ArmorMeta)) {
            return null;
        }
        
        ArmorMeta meta = (ArmorMeta) armorPiece.getItemMeta();
        ArmorTrim trim = meta.getTrim();
        
        return trim != null ? trim.getPattern().getKey().getKey() : null;
    }
    
    /**
     * Get the trim material name from an armor piece
     * 
     * @param armorPiece The armor piece to check
     * @return The trim material name, or null if no trim
     */
    public static String getTrimMaterial(ItemStack armorPiece) {
        if (armorPiece == null || !(armorPiece.getItemMeta() instanceof ArmorMeta)) {
            return null;
        }
        
        ArmorMeta meta = (ArmorMeta) armorPiece.getItemMeta();
        ArmorTrim trim = meta.getTrim();
        
        return trim != null ? trim.getMaterial().getKey().getKey() : null;
    }
    
    /**
     * Check if a player has a full set of the same trim
     * 
     * @param player The player to check
     * @return The trim name if player has a full set, null otherwise
     */
    public static String getFullSetTrim(Player player) {
        if (player == null) return null;
        
        ItemStack[] armor = player.getInventory().getArmorContents();
        String firstTrim = null;
        
        for (ItemStack item : armor) {
            if (item == null || !(item.getItemMeta() instanceof ArmorMeta)) {
                return null; // Missing armor piece
            }
            
            ArmorMeta meta = (ArmorMeta) item.getItemMeta();
            ArmorTrim trim = meta.getTrim();
            
            if (trim == null) {
                return null; // Missing trim
            }
            
            String patternName = trim.getPattern().getKey().getKey();
            String materialName = trim.getMaterial().getKey().getKey();
            String trimKey = patternName + "_" + materialName;

            if (firstTrim == null) {
                firstTrim = trimKey;
            } else if (!firstTrim.equals(trimKey)) {
                return null; // Different trims
            }
        }
        
        return firstTrim;
    }
    
    /**
     * Get all active trims for a player
     * 
     * @param player The player to check
     * @return Set of active trim names
     */
    public static Set<String> getActiveTrims(Player player) {
        if (player == null) return new HashSet<>();
        
        PoweredTrims plugin = PoweredTrims.getInstance();
        if (plugin == null) return new HashSet<>();
        
        return plugin.getTrimManager().getPlayerActiveTrims(player);
    }
    
    /**
     * Check if a trim is enabled in the configuration
     * 
     * @param trimName The trim name to check
     * @return true if the trim is enabled, false otherwise
     */
    public static boolean isTrimEnabled(String trimName) {
        if (trimName == null) return false;
        
        PoweredTrims plugin = PoweredTrims.getInstance();
        if (plugin == null) return false;
        
        TrimConfig trimConfig = plugin.getConfigManager().getTrimConfig(trimName);
        return trimConfig != null && trimConfig.isEnabled();
    }
    
    /**
     * Get the configuration for a specific trim
     * 
     * @param trimName The trim name
     * @return The trim configuration, or null if not found
     */
    public static TrimConfig getTrimConfig(String trimName) {
        if (trimName == null) return null;
        
        PoweredTrims plugin = PoweredTrims.getInstance();
        if (plugin == null) return null;
        
        return plugin.getConfigManager().getTrimConfig(trimName);
    }
    
    /**
     * Force update a player's trim effects
     * 
     * @param player The player to update
     */
    public static void updatePlayerTrims(Player player) {
        if (player == null) return;
        
        PoweredTrims plugin = PoweredTrims.getInstance();
        if (plugin == null) return;
        
        plugin.getTrimManager().updatePlayerTrims(player);
    }
    
    /**
     * Check if MythicMobs integration is available
     * 
     * @return true if MythicMobs is available, false otherwise
     */
    public static boolean isMythicMobsAvailable() {
        PoweredTrims plugin = PoweredTrims.getInstance();
        return plugin != null && plugin.isMythicMobsEnabled();
    }
}
