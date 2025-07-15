package com.poweredtrims.managers;

import com.poweredtrims.PoweredTrims;
import com.poweredtrims.config.TrimConfig;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrimManager {
    
    private final PoweredTrims plugin;
    private final Map<UUID, Map<String, AttributeModifier>> playerModifiers = new ConcurrentHashMap<>();
    private final Map<UUID, Set<PotionEffect>> playerEffects = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> playerActiveTrims = new ConcurrentHashMap<>();
    
    public TrimManager(PoweredTrims plugin) {
        this.plugin = plugin;
    }
    
    public void updatePlayerTrims(Player player) {
        if (player == null || !player.isOnline()) return;
        
        // Clear existing modifiers and effects
        clearPlayerModifiers(player);
        clearPlayerEffects(player);
        
        // Get all equipped armor pieces with trims
        Map<String, String> equippedTrims = getEquippedTrims(player);
        Set<String> activeTrims = new HashSet<>();
        
        // Apply individual trim effects
        for (Map.Entry<String, String> entry : equippedTrims.entrySet()) {
            String slot = entry.getKey();
            String trimName = entry.getValue();
            
            TrimConfig trimConfig = plugin.getConfigManager().getTrimConfig(trimName);
            if (trimConfig == null || !trimConfig.isEnabled()) continue;
            
            activeTrims.add(trimName);
            
            // Apply attributes
            applyTrimAttributes(player, trimConfig, slot);
            
            // Apply effects
            applyTrimEffects(player, trimConfig);
            
            // Trigger equip skill
            if (trimConfig.hasOnEquipSkill() && plugin.isMythicMobsEnabled()) {
                plugin.getMythicSkillManager().executeSkill(player, trimConfig.getOnEquipSkill());
            }
        }
        
        // Check for set bonus
        checkSetBonus(player, equippedTrims);
        
        // Update player's active trims
        playerActiveTrims.put(player.getUniqueId(), activeTrims);
        
        // Update lore if enabled
        if (plugin.getConfigManager().isLoreEnabled()) {
            plugin.getLoreManager().updatePlayerArmorLore(player);
        }
    }
    
    private Map<String, String> getEquippedTrims(Player player) {
        Map<String, String> trims = new HashMap<>();

        ItemStack[] armor = player.getInventory().getArmorContents();
        String[] slots = {"boots", "leggings", "chestplate", "helmet"};

        for (int i = 0; i < armor.length; i++) {
            ItemStack item = armor[i];
            if (item == null || !(item.getItemMeta() instanceof ArmorMeta)) continue;

            ArmorMeta meta = (ArmorMeta) item.getItemMeta();
            ArmorTrim trim = meta.getTrim();

            if (trim != null) {
                String patternName = trim.getPattern().getKey().getKey();
                String materialName = trim.getMaterial().getKey().getKey();
                String trimKey = patternName + "_" + materialName;
                trims.put(slots[i], trimKey);
            }
        }

        return trims;
    }
    
    private void applyTrimAttributes(Player player, TrimConfig trimConfig, String slot) {
        Map<String, Double> attributes = trimConfig.getAttributes();
        
        for (Map.Entry<String, Double> entry : attributes.entrySet()) {
            String attrName = entry.getKey();
            double value = entry.getValue();
            
            try {
                Attribute attribute = Attribute.valueOf(attrName);
                AttributeInstance attrInstance = player.getAttribute(attribute);
                
                if (attrInstance != null) {
                    String modifierName = "PoweredTrims_" + trimConfig.getName() + "_" + slot;
                    UUID modifierUUID = UUID.nameUUIDFromBytes(modifierName.getBytes());
                    
                    AttributeModifier modifier = new AttributeModifier(
                        modifierUUID,
                        modifierName,
                        value,
                        AttributeModifier.Operation.ADD_NUMBER
                    );
                    
                    attrInstance.addModifier(modifier);
                    
                    // Store modifier for cleanup
                    playerModifiers.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                        .put(modifierName, modifier);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid attribute: " + attrName + " for trim: " + trimConfig.getName());
            }
        }
    }
    
    private void applyTrimEffects(Player player, TrimConfig trimConfig) {
        Map<String, TrimConfig.EffectData> effects = trimConfig.getEffects();

        for (TrimConfig.EffectData effectData : effects.values()) {
            try {
                PotionEffectType effectType = PotionEffectType.getByName(effectData.getType());
                if (effectType != null) {
                    // Permanent effect with very long duration (999999 ticks = ~13.8 hours)
                    PotionEffect effect = new PotionEffect(
                        effectType,
                        999999,
                        effectData.getAmplifier(),
                        false,
                        false,
                        true
                    );

                    player.addPotionEffect(effect, true);

                    // Store effect for cleanup
                    playerEffects.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>())
                        .add(effect);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid effect: " + effectData.getType() + " for trim: " + trimConfig.getName());
            }
        }
    }
    
    private void checkSetBonus(Player player, Map<String, String> equippedTrims) {
        if (equippedTrims.size() < 4) return;

        // Check if all 4 pieces have the same trim (pattern_material)
        String firstTrim = null;
        boolean isFullSet = true;

        for (String trimKey : equippedTrims.values()) {
            if (firstTrim == null) {
                firstTrim = trimKey;
            } else if (!firstTrim.equals(trimKey)) {
                isFullSet = false;
                break;
            }
        }

        if (isFullSet && firstTrim != null) {
            TrimConfig trimConfig = plugin.getConfigManager().getTrimConfig(firstTrim);
            if (trimConfig != null && trimConfig.hasSetBonusSkill() && plugin.isMythicMobsEnabled()) {
                plugin.getMythicSkillManager().executeSkill(player, trimConfig.getSetBonusSkill());
            }
        }
    }
    
    private void clearPlayerModifiers(Player player) {
        Map<String, AttributeModifier> modifiers = playerModifiers.get(player.getUniqueId());
        if (modifiers == null) return;
        
        for (Map.Entry<String, AttributeModifier> entry : modifiers.entrySet()) {
            AttributeModifier modifier = entry.getValue();
            
            // Find the attribute and remove the modifier
            for (Attribute attribute : Attribute.values()) {
                AttributeInstance attrInstance = player.getAttribute(attribute);
                if (attrInstance != null) {
                    attrInstance.removeModifier(modifier);
                }
            }
        }
        
        modifiers.clear();
    }
    
    private void clearPlayerEffects(Player player) {
        Set<PotionEffect> effects = playerEffects.get(player.getUniqueId());
        if (effects == null) return;
        
        for (PotionEffect effect : effects) {
            player.removePotionEffect(effect.getType());
        }
        
        effects.clear();
    }
    
    public void removePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        clearPlayerModifiers(player);
        clearPlayerEffects(player);
        
        playerModifiers.remove(playerId);
        playerEffects.remove(playerId);
        playerActiveTrims.remove(playerId);
    }
    
    public Set<String> getPlayerActiveTrims(Player player) {
        return playerActiveTrims.getOrDefault(player.getUniqueId(), new HashSet<>());
    }
    
    public void reload() {
        // Clear all player data and reapply
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayerTrims(player);
        }
    }
    
    public void cleanup() {
        // Clean up all players
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            removePlayer(player);
        }
        
        playerModifiers.clear();
        playerEffects.clear();
        playerActiveTrims.clear();
    }
}
