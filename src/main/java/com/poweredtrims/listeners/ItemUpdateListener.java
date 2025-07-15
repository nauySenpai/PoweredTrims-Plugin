package com.poweredtrims.listeners;

import com.poweredtrims.PoweredTrims;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener to detect when players get new items and update lore accordingly
 */
public class ItemUpdateListener implements Listener {
    
    private final PoweredTrims plugin;
    
    public ItemUpdateListener(PoweredTrims plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Check and fix all armor items when player joins
        new BukkitRunnable() {
            @Override
            public void run() {
                fixPlayerArmorLore(event.getPlayer());
            }
        }.runTaskLater(plugin, 20L); // Wait 1 second after join
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // Check after inventory change
        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndUpdatePlayerArmor(player);
            }
        }.runTaskLater(plugin, 1L);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // Check after inventory change
        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndUpdatePlayerArmor(player);
            }
        }.runTaskLater(plugin, 1L);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();
        
        // Check if picked up item is armor with trim
        if (isArmorWithTrim(item)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    checkAndUpdatePlayerArmor(player);
                }
            }.runTaskLater(plugin, 1L);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        // Check armor when player changes held item (might have moved armor around)
        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndUpdatePlayerArmor(event.getPlayer());
            }
        }.runTaskLater(plugin, 1L);
    }
    
    /**
     * Fix and update all armor items for a player (force update on login)
     */
    private void fixPlayerArmorLore(Player player) {
        if (player == null || !player.isOnline()) return;

        // Force update equipped armor
        plugin.getLoreManager().updatePlayerArmorLore(player, true);

        // Force update inventory armor items
        plugin.getLoreManager().updatePlayerInventoryLore(player, true);

        // Also trigger trim effects update
        plugin.getTrimManager().updatePlayerTrims(player);
    }

    /**
     * Check and update all armor items for a player (only if missing lore)
     */
    private void checkAndUpdatePlayerArmor(Player player) {
        if (player == null || !player.isOnline()) return;

        ItemStack[] armor = player.getInventory().getArmorContents();
        String[] slots = {"boots", "leggings", "chestplate", "helmet"};
        boolean updated = false;

        for (int i = 0; i < armor.length; i++) {
            ItemStack item = armor[i];
            if (item == null || !(item.getItemMeta() instanceof ArmorMeta)) continue;

            ArmorMeta meta = (ArmorMeta) item.getItemMeta();
            if (meta.getTrim() == null) continue;

            // Check if item already has PoweredTrims lore
            if (!plugin.getLoreManager().hasPoweredTrimsLore(meta)) {
                // Add lore if not present
                if (plugin.getLoreManager().updateItemLore(item, slots[i])) {
                    updated = true;
                }
            }
        }

        // Also check inventory for armor items
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !(item.getItemMeta() instanceof ArmorMeta)) continue;

            ArmorMeta meta = (ArmorMeta) item.getItemMeta();
            if (meta.getTrim() == null) continue;

            // Check if item already has PoweredTrims lore
            if (!plugin.getLoreManager().hasPoweredTrimsLore(meta)) {
                // Determine slot type
                String slot = getArmorSlot(item);
                if (slot != null) {
                    plugin.getLoreManager().updateItemLore(item, slot);
                }
            }
        }

        if (updated) {
            player.getInventory().setArmorContents(armor);
        }
    }
    
    /**
     * Check if item is armor with trim
     */
    private boolean isArmorWithTrim(ItemStack item) {
        if (item == null || !(item.getItemMeta() instanceof ArmorMeta)) return false;
        
        ArmorMeta meta = (ArmorMeta) item.getItemMeta();
        return meta.getTrim() != null;
    }
    
    /**
     * Get armor slot name from item type
     */
    private String getArmorSlot(ItemStack item) {
        String typeName = item.getType().name().toLowerCase();
        
        if (typeName.contains("helmet")) return "helmet";
        if (typeName.contains("chestplate")) return "chestplate";
        if (typeName.contains("leggings")) return "leggings";
        if (typeName.contains("boots")) return "boots";
        
        return null;
    }
}
