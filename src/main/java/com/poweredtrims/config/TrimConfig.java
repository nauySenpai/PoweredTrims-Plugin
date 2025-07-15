package com.poweredtrims.config;

import java.util.HashMap;
import java.util.Map;

public class TrimConfig {
    
    private final String name;
    private boolean enabled = true;
    
    // Attributes
    private final Map<String, Double> attributes = new HashMap<>();
    
    // Effects
    private final Map<String, EffectData> effects = new HashMap<>();
    
    // Mythic Skills
    private String onEquipSkill;
    private String onUnequipSkill;
    private String setBonusSkill;
    
    // Lore settings
    private boolean loreEnabled = true;
    private String loreTemplate = "default";
    
    public TrimConfig(String name) {
        this.name = name;
    }
    
    // Basic getters/setters
    public String getName() {
        return name;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    // Attribute methods
    public void addAttribute(String attributeName, double value) {
        attributes.put(attributeName.toUpperCase(), value);
    }
    
    public Map<String, Double> getAttributes() {
        return new HashMap<>(attributes);
    }
    
    public double getAttributeValue(String attributeName) {
        return attributes.getOrDefault(attributeName.toUpperCase(), 0.0);
    }
    
    public boolean hasAttribute(String attributeName) {
        return attributes.containsKey(attributeName.toUpperCase());
    }
    
    // Effect methods
    public void addEffect(String effectType, int amplifier, int duration) {
        effects.put(effectType.toUpperCase(), new EffectData(effectType, amplifier, duration));
    }
    
    public Map<String, EffectData> getEffects() {
        return new HashMap<>(effects);
    }
    
    public EffectData getEffect(String effectType) {
        return effects.get(effectType.toUpperCase());
    }
    
    public boolean hasEffect(String effectType) {
        return effects.containsKey(effectType.toUpperCase());
    }
    
    // Skill methods
    public void setSkills(String onEquip, String onUnequip, String setBonus) {
        this.onEquipSkill = onEquip;
        this.onUnequipSkill = onUnequip;
        this.setBonusSkill = setBonus;
    }
    
    public String getOnEquipSkill() {
        return onEquipSkill;
    }
    
    public String getOnUnequipSkill() {
        return onUnequipSkill;
    }
    
    public String getSetBonusSkill() {
        return setBonusSkill;
    }
    
    public boolean hasOnEquipSkill() {
        return onEquipSkill != null && !onEquipSkill.isEmpty();
    }
    
    public boolean hasOnUnequipSkill() {
        return onUnequipSkill != null && !onUnequipSkill.isEmpty();
    }
    
    public boolean hasSetBonusSkill() {
        return setBonusSkill != null && !setBonusSkill.isEmpty();
    }
    
    // Lore methods
    public boolean isLoreEnabled() {
        return loreEnabled;
    }
    
    public void setLoreEnabled(boolean loreEnabled) {
        this.loreEnabled = loreEnabled;
    }
    
    public String getLoreTemplate() {
        return loreTemplate;
    }
    
    public void setLoreTemplate(String loreTemplate) {
        this.loreTemplate = loreTemplate;
    }
    
    // Inner class for effect data
    public static class EffectData {
        private final String type;
        private final int amplifier;
        private final int duration;
        
        public EffectData(String type, int amplifier, int duration) {
            this.type = type;
            this.amplifier = amplifier;
            this.duration = duration;
        }
        
        public String getType() {
            return type;
        }
        
        public int getAmplifier() {
            return amplifier;
        }
        
        public int getDuration() {
            return duration;
        }
    }
}
