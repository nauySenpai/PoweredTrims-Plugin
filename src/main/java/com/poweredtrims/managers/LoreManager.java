package com.poweredtrims.managers;

import com.poweredtrims.PoweredTrims;
import com.poweredtrims.config.TrimConfig;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoreManager {
    
    private final PoweredTrims plugin;
    private final NamespacedKey loreKey;
    private final Pattern placeholderPattern = Pattern.compile("%([^%]+)%");
    
    public LoreManager(PoweredTrims plugin) {
        this.plugin = plugin;
        this.loreKey = new NamespacedKey(plugin, "poweredtrims_lore");
    }
    
    public void updatePlayerArmorLore(Player player) {
        updatePlayerArmorLore(player, false);
    }

    public void updatePlayerArmorLore(Player player, boolean forceUpdate) {
        if (!plugin.getConfigManager().isLoreEnabled()) return;

        ItemStack[] armor = player.getInventory().getArmorContents();
        String[] slots = {"boots", "leggings", "chestplate", "helmet"};
        boolean updated = false;

        for (int i = 0; i < armor.length; i++) {
            ItemStack item = armor[i];
            if (item == null || !(item.getItemMeta() instanceof ArmorMeta)) continue;

            // Update lore (force update if requested)
            if (updateItemLore(item, slots[i], forceUpdate)) {
                updated = true;
            }
        }

        if (updated) {
            player.getInventory().setArmorContents(armor);
        }
    }
    
    public boolean updateItemLore(ItemStack item, String slot) {
        return updateItemLore(item, slot, false);
    }

    public boolean updateItemLore(ItemStack item, String slot, boolean forceUpdate) {
        if (item == null || !(item.getItemMeta() instanceof ArmorMeta)) return false;

        ArmorMeta meta = (ArmorMeta) item.getItemMeta();

        // Check if item already has PoweredTrims lore - if yes, don't add again (unless forced)
        if (!forceUpdate && hasPoweredTrimsLore(meta)) {
            return false; // Already has lore, no need to update
        }

        // Always remove existing PoweredTrims lore before adding new
        removePoweredTrimsLore(meta);

        ArmorTrim trim = meta.getTrim();

        if (trim == null) {
            // Don't add lore for items without trim
            return false;
        }

        String patternName = trim.getPattern().getKey().getKey();
        String materialName = trim.getMaterial().getKey().getKey();
        String trimKey = patternName + "_" + materialName;
        TrimConfig trimConfig = plugin.getConfigManager().getTrimConfig(trimKey);

        if (trimConfig == null || !trimConfig.isEnabled() || !trimConfig.isLoreEnabled()) {
            // Show basic trim info even if not configured
            List<String> basicLore = Arrays.asList(
                "&8Trim: " + formatTrimName(patternName) + " (" + formatTrimName(materialName) + ")",
                "&7Not configured"
            );
            applyLoreToItem(meta, basicLore);
            item.setItemMeta(meta);
            return true;
        }

        // Generate new lore
        List<String> newLore = generateTrimLore(trimConfig, slot);

        // Apply lore to item
        applyLoreToItem(meta, newLore);
        item.setItemMeta(meta);
        return true;
    }
    
    private List<String> generateTrimLore(TrimConfig trimConfig, String slot) {
        List<String> template;
        
        if (plugin.getConfigManager().useLoreFormatFile()) {
            template = plugin.getConfigManager().getLoreTemplate(trimConfig.getLoreTemplate());
            if (template == null) {
                template = plugin.getConfigManager().getLoreTemplate("default");
            }
        } else {
            template = Arrays.asList(
                "&7[&a+%attr:GENERIC_ARMOR% Armor&7]",
                "&7[&a+%attr:GENERIC_ATTACK_SPEED% Attack Speed&7]",
                "&8Trim: %trim_name%"
            );
        }
        
        if (template == null) {
            return new ArrayList<>();
        }
        
        List<String> processedLore = new ArrayList<>();

        for (String line : template) {
            String processedLine = processPlaceholders(line, trimConfig, slot);
            if (processedLine != null && !processedLine.trim().isEmpty()) {
                // Check if this line contains the effect_lines placeholder
                if (processedLine.contains("EFFECT_LINES_PLACEHOLDER")) {
                    // Replace with individual effect lines
                    List<String> effectLines = generateEffectLines(line, trimConfig);
                    for (String effectLine : effectLines) {
                        processedLore.add(ChatColor.translateAlternateColorCodes('&', effectLine));
                    }
                } else {
                    processedLore.add(ChatColor.translateAlternateColorCodes('&', processedLine));
                }
            }
        }

        return processedLore;
    }
    
    private String processPlaceholders(String line, TrimConfig trimConfig, String slot) {
        Matcher matcher = placeholderPattern.matcher(line);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = getPlaceholderValue(placeholder, trimConfig, slot);
            
            if (replacement == null) {
                // If placeholder can't be resolved, skip this line
                return null;
            }
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private String getPlaceholderValue(String placeholder, TrimConfig trimConfig, String slot) {
        switch (placeholder.toLowerCase()) {
            case "trim_name":
                return trimConfig.getName();
            case "trim_display":
                return formatTrimName(trimConfig.getName());
            case "slot":
                return slot;
            case "effects":
                return formatEffects(trimConfig);
            case "effect_lines":
                return "EFFECT_LINES_PLACEHOLDER"; // Special marker for multi-line processing
            default:
                // Handle attribute placeholders
                if (placeholder.startsWith("attr:")) {
                    String attrName = placeholder.substring(5);
                    double value = trimConfig.getAttributeValue(attrName);
                    return value > 0 ? String.format("%.1f", value) : null;
                }

                // Handle skill placeholders
                if (placeholder.startsWith("skill:")) {
                    String skillType = placeholder.substring(6);
                    return getSkillPlaceholder(skillType, trimConfig);
                }

                return null;
        }
    }
    
    private String getSkillPlaceholder(String skillType, TrimConfig trimConfig) {
        switch (skillType.toLowerCase()) {
            case "onequip":
                return trimConfig.hasOnEquipSkill() ? trimConfig.getOnEquipSkill() : null;
            case "onunequip":
                return trimConfig.hasOnUnequipSkill() ? trimConfig.getOnUnequipSkill() : null;
            case "setbonus:onequip":
                return trimConfig.hasSetBonusSkill() ? trimConfig.getSetBonusSkill() : null;
            default:
                return null;
        }
    }
    
    private String formatTrimName(String trimName) {
        return Arrays.stream(trimName.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce((a, b) -> a + " " + b)
                .orElse(trimName);
    }

    /**
     * Check if item already has PoweredTrims lore
     */
    public boolean hasPoweredTrimsLore(ItemMeta meta) {
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(loreKey, PersistentDataType.BYTE);
    }
    
    private String formatEffects(TrimConfig trimConfig) {
        Map<String, TrimConfig.EffectData> effects = trimConfig.getEffects();
        if (effects.isEmpty()) return null;
        
        List<String> effectStrings = new ArrayList<>();
        for (TrimConfig.EffectData effect : effects.values()) {
            String effectName = formatEffectName(effect.getType());
            String level = effect.getAmplifier() > 0 ? " " + (effect.getAmplifier() + 1) : "";
            effectStrings.add(effectName + level);
        }
        
        return String.join(", ", effectStrings);
    }
    
    private String formatEffectName(String effectType) {
        return Arrays.stream(effectType.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce((a, b) -> a + " " + b)
                .orElse(effectType);
    }

    private List<String> generateEffectLines(String templateLine, TrimConfig trimConfig) {
        List<String> effectLines = new ArrayList<>();
        Map<String, TrimConfig.EffectData> effects = trimConfig.getEffects();

        if (effects.isEmpty()) {
            return effectLines; // Return empty list if no effects
        }

        for (TrimConfig.EffectData effect : effects.values()) {
            String effectName = formatEffectName(effect.getType());
            String level = effect.getAmplifier() > 0 ? " " + (effect.getAmplifier() + 1) : "";
            String effectDisplay = effectName + level;

            // Replace the placeholder with individual effect
            String effectLine = templateLine.replace("%effect_lines%", effectDisplay);
            effectLines.add(effectLine);
        }

        return effectLines;
    }
    
    private void applyLoreToItem(ItemMeta meta, List<String> newLore) {
        List<String> currentLore = meta.getLore();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }
        
        // Remove existing PoweredTrims lore
        removePoweredTrimsLore(meta);
        currentLore = meta.getLore();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }
        
        // Add new PoweredTrims lore
        if (!newLore.isEmpty()) {
            currentLore.addAll(newLore);
            meta.setLore(currentLore);
            
            // Mark as having PoweredTrims lore
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(loreKey, PersistentDataType.BYTE, (byte) 1);
        }
    }
    
    private void removePoweredTrimsLore(ItemMeta meta) {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        
        if (!pdc.has(loreKey, PersistentDataType.BYTE)) {
            return; // No PoweredTrims lore to remove
        }
        
        // Remove the marker
        pdc.remove(loreKey);
        
        // Remove PoweredTrims lore lines using improved detection
        List<String> currentLore = meta.getLore();
        if (currentLore != null) {
            // Remove lines that look like PoweredTrims lore (improved heuristic)
            currentLore.removeIf(line -> {
                String stripped = ChatColor.stripColor(line).toLowerCase();
                return stripped.contains("trim:") ||
                       stripped.contains("armor:") ||
                       stripped.contains("attack speed:") ||
                       stripped.contains("movement speed:") ||
                       stripped.contains("skill:") ||
                       stripped.contains("effects:") ||
                       stripped.contains("not configured") ||
                       // Effect names
                       stripped.contains("speed") || stripped.contains("strength") ||
                       stripped.contains("regeneration") || stripped.contains("resistance") ||
                       stripped.contains("fire resistance") || stripped.contains("water breathing") ||
                       stripped.contains("night vision") || stripped.contains("invisibility") ||
                       stripped.contains("jump boost") || stripped.contains("haste") ||
                       stripped.contains("mining fatigue") || stripped.contains("nausea") ||
                       stripped.contains("blindness") || stripped.contains("hunger") ||
                       stripped.contains("weakness") || stripped.contains("poison") ||
                       stripped.contains("wither") || stripped.contains("health boost") ||
                       stripped.contains("absorption") || stripped.contains("saturation") ||
                       stripped.contains("glowing") || stripped.contains("levitation") ||
                       stripped.contains("luck") || stripped.contains("unluck") ||
                       stripped.contains("slow falling") || stripped.contains("conduit power") ||
                       stripped.contains("dolphins grace") || stripped.contains("bad omen") ||
                       stripped.contains("hero of the village") || stripped.contains("darkness") ||
                       line.contains("⚡") || line.contains("🛡") ||
                       line.contains("⚔") || line.contains("💨") ||
                       line.contains("❤") || line.contains("⭐") ||
                       line.contains("🌊") || line.contains("🔥") ||
                       line.contains("🔮") || line.contains("🌿") ||
                       line.contains("🤫") || line.contains("🧭") ||
                       line.contains("🔨") || line.contains("💀") ||
                       line.contains("🦘") || line.contains("🏜") ||
                       line.contains("👁") || line.contains("🛡️");
            });

            if (currentLore.isEmpty()) {
                meta.setLore(null);
            } else {
                meta.setLore(currentLore);
            }
        }
    }
    
    public void reload() {
        // Force reload lore for all online players
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayerArmorLore(player, true); // Force update
            updatePlayerInventoryLore(player, true); // Also update inventory items
        }
    }

    /**
     * Update lore for all armor items in player's inventory (not just equipped)
     */
    public void updatePlayerInventoryLore(Player player, boolean forceUpdate) {
        if (!plugin.getConfigManager().isLoreEnabled()) return;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !(item.getItemMeta() instanceof ArmorMeta)) continue;

            ArmorMeta meta = (ArmorMeta) item.getItemMeta();
            if (meta.getTrim() == null) continue;

            // Determine slot type
            String slot = getArmorSlot(item);
            if (slot != null) {
                updateItemLore(item, slot, forceUpdate);
            }
        }
    }

    /**
     * Get armor slot type from item
     */
    private String getArmorSlot(ItemStack item) {
        if (item == null) return null;

        String typeName = item.getType().name().toLowerCase();
        if (typeName.contains("helmet")) return "helmet";
        if (typeName.contains("chestplate")) return "chestplate";
        if (typeName.contains("leggings")) return "leggings";
        if (typeName.contains("boots")) return "boots";

        return null;
    }
}
