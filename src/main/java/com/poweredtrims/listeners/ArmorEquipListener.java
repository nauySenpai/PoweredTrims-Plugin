package com.poweredtrims.listeners;

import com.poweredtrims.PoweredTrims;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class ArmorEquipListener implements Listener {
    
    private final PoweredTrims plugin;
    
    public ArmorEquipListener(PoweredTrims plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Delay the trim update to ensure player is fully loaded
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getTrimManager().updatePlayerTrims(player);
        }, 20L); // 1 second delay
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getTrimManager().removePlayer(player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // Check if the click affects armor slots
        if (isArmorSlotAffected(event)) {
            // Delay the update to ensure inventory changes are processed
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getTrimManager().updatePlayerTrims(player);
            }, 1L);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // Check if the drag affects armor slots
        if (isDragAffectingArmorSlots(event)) {
            // Delay the update to ensure inventory changes are processed
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getTrimManager().updatePlayerTrims(player);
            }, 1L);
        }
    }
    
    private boolean isArmorSlotAffected(InventoryClickEvent event) {
        // Check if clicking in player inventory
        if (event.getInventory().getType() != InventoryType.PLAYER && 
            event.getInventory().getType() != InventoryType.CRAFTING) {
            return false;
        }
        
        int slot = event.getSlot();
        
        // Armor slots in player inventory: 36-39 (boots, leggings, chestplate, helmet)
        if (slot >= 36 && slot <= 39) {
            return true;
        }
        
        // Check if moving armor item
        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        
        return isArmorItem(currentItem) || isArmorItem(cursorItem);
    }
    
    private boolean isDragAffectingArmorSlots(InventoryDragEvent event) {
        // Check if any of the drag slots are armor slots
        for (int slot : event.getRawSlots()) {
            if (slot >= 36 && slot <= 39) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isArmorItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        
        String typeName = item.getType().name();
        return typeName.endsWith("_HELMET") || 
               typeName.endsWith("_CHESTPLATE") || 
               typeName.endsWith("_LEGGINGS") || 
               typeName.endsWith("_BOOTS");
    }
}
