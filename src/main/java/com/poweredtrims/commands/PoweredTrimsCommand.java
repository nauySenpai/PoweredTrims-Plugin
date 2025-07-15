package com.poweredtrims.commands;

import com.poweredtrims.PoweredTrims;
import com.poweredtrims.config.TrimConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.*;

public class PoweredTrimsCommand implements CommandExecutor, TabCompleter {
    
    private final PoweredTrims plugin;
    
    public PoweredTrimsCommand(PoweredTrims plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("poweredtrims.reload") && !sender.hasPermission("poweredtrims.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                return handleReload(sender);
            case "stats":
                if (!sender.hasPermission("poweredtrims.stats") && !sender.hasPermission("poweredtrims.view")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                return handleStats(sender);
            case "debug":
                if (!sender.hasPermission("poweredtrims.debug") && !sender.hasPermission("poweredtrims.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                return handleDebug(sender);
            case "fixlore":
                if (!sender.hasPermission("poweredtrims.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                return handleFixLore(sender);
            case "help":
            default:
                sendHelpMessage(sender);
                return true;
        }
    }
    
    private boolean handleReload(CommandSender sender) {
        // Permission already checked in onCommand
        try {
            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "PoweredTrims has been reloaded successfully!");
            
            // Validate MythicMobs skills if available
            if (plugin.isMythicMobsEnabled()) {
                plugin.getMythicSkillManager().validateConfiguredSkills();
            }
            
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error reloading PoweredTrims: " + e.getMessage());
            plugin.getLogger().severe("Error during reload: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleStats(CommandSender sender) {
        // Permission already checked in onCommand
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        sender.sendMessage(ChatColor.GOLD + "=== PoweredTrims Stats ===");
        
        // Get equipped armor with trims
        ItemStack[] armor = player.getInventory().getArmorContents();
        String[] slots = {"Boots", "Leggings", "Chestplate", "Helmet"};
        
        boolean hasAnyTrims = false;
        Map<String, Integer> trimCounts = new HashMap<>();
        
        for (int i = 0; i < armor.length; i++) {
            ItemStack item = armor[i];
            if (item == null || !(item.getItemMeta() instanceof ArmorMeta)) continue;
            
            ArmorMeta meta = (ArmorMeta) item.getItemMeta();
            ArmorTrim trim = meta.getTrim();
            
            if (trim != null) {
                hasAnyTrims = true;
                String patternName = trim.getPattern().getKey().getKey();
                String materialName = trim.getMaterial().getKey().getKey();
                String trimKey = patternName + "_" + materialName;

                trimCounts.put(trimKey, trimCounts.getOrDefault(trimKey, 0) + 1);

                TrimConfig trimConfig = plugin.getConfigManager().getTrimConfig(trimKey);
                
                sender.sendMessage(ChatColor.YELLOW + slots[i] + ": " +
                    ChatColor.WHITE + formatTrimName(patternName) +
                    ChatColor.GRAY + " (" + formatTrimName(materialName) + ")");
                
                if (trimConfig != null && trimConfig.isEnabled()) {
                    // Show attributes
                    Map<String, Double> attributes = trimConfig.getAttributes();
                    if (!attributes.isEmpty()) {
                        sender.sendMessage(ChatColor.GREEN + "  Attributes:");
                        for (Map.Entry<String, Double> entry : attributes.entrySet()) {
                            sender.sendMessage(ChatColor.GREEN + "    +" + entry.getValue() + " " + 
                                formatAttributeName(entry.getKey()));
                        }
                    }
                    
                    // Show effects
                    Map<String, TrimConfig.EffectData> effects = trimConfig.getEffects();
                    if (!effects.isEmpty()) {
                        sender.sendMessage(ChatColor.BLUE + "  Effects:");
                        for (TrimConfig.EffectData effect : effects.values()) {
                            sender.sendMessage(ChatColor.BLUE + "    " + 
                                formatEffectName(effect.getType()) + " " + 
                                (effect.getAmplifier() + 1) + " (" + 
                                (effect.getDuration() / 20) + "s)");
                        }
                    }
                    
                    // Show skills
                    if (trimConfig.hasOnEquipSkill()) {
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "  Skill: " + trimConfig.getOnEquipSkill());
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "  (Disabled or not configured)");
                }
            }
        }
        
        if (!hasAnyTrims) {
            sender.sendMessage(ChatColor.GRAY + "No armor trims equipped.");
        } else {
            // Check for set bonuses
            sender.sendMessage(ChatColor.GOLD + "\nSet Bonuses:");
            for (Map.Entry<String, Integer> entry : trimCounts.entrySet()) {
                String trimKey = entry.getKey();
                int count = entry.getValue();

                if (count == 4) {
                    TrimConfig trimConfig = plugin.getConfigManager().getTrimConfig(trimKey);
                    if (trimConfig != null && trimConfig.hasSetBonusSkill()) {
                        sender.sendMessage(ChatColor.GOLD + "  " + formatTrimKey(trimKey) +
                            " Set (4/4): " + ChatColor.LIGHT_PURPLE + trimConfig.getSetBonusSkill());
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "  " + formatTrimKey(trimKey) +
                            " Set (4/4): No bonus configured");
                    }
                } else {
                    sender.sendMessage(ChatColor.GRAY + "  " + formatTrimKey(trimKey) +
                        " Set (" + count + "/4)");
                }
            }
        }
        
        return true;
    }
    
    private boolean handleDebug(CommandSender sender) {
        // Permission already checked in onCommand
        sender.sendMessage(ChatColor.GOLD + "=== PoweredTrims Debug Info ===");
        sender.sendMessage(ChatColor.YELLOW + "Plugin Version: " + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "MythicMobs Integration: " + 
            (plugin.isMythicMobsEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        
        // Config info
        sender.sendMessage(ChatColor.YELLOW + "Lore Enabled: " +
            (plugin.getConfigManager().isLoreEnabled() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        sender.sendMessage(ChatColor.YELLOW + "Use Lore Format File: " +
            (plugin.getConfigManager().useLoreFormatFile() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        
        // Trim configs
        Map<String, TrimConfig> trimConfigs = plugin.getConfigManager().getAllTrimConfigs();
        sender.sendMessage(ChatColor.YELLOW + "Loaded Trim Configs: " + trimConfigs.size());
        
        for (Map.Entry<String, TrimConfig> entry : trimConfigs.entrySet()) {
            TrimConfig config = entry.getValue();
            sender.sendMessage(ChatColor.WHITE + "  " + entry.getKey() + ": " + 
                (config.isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        }
        
        // Lore templates
        Map<String, List<String>> templates = plugin.getConfigManager().getAllLoreTemplates();
        sender.sendMessage(ChatColor.YELLOW + "Loaded Lore Templates: " + templates.size());
        
        for (String templateName : templates.keySet()) {
            sender.sendMessage(ChatColor.WHITE + "  " + templateName);
        }
        
        return true;
    }

    private boolean handleFixLore(CommandSender sender) {
        try {
            int playersFixed = 0;

            // Fix lore for all online players
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                plugin.getLoreManager().updatePlayerArmorLore(player, true);
                plugin.getLoreManager().updatePlayerInventoryLore(player, true);
                playersFixed++;
            }

            sender.sendMessage(ChatColor.GREEN + "Fixed lore for " + playersFixed + " online players!");
            sender.sendMessage(ChatColor.YELLOW + "All armor items with trims have been updated with correct lore.");

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error fixing lore: " + e.getMessage());
            plugin.getLogger().severe("Error during lore fix: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== PoweredTrims Commands ===");

        // Show stats command (available to everyone with poweredtrims.view)
        if (sender.hasPermission("poweredtrims.stats") || sender.hasPermission("poweredtrims.view")) {
            sender.sendMessage(ChatColor.YELLOW + "/pt stats" + ChatColor.WHITE + " - Show plugin statistics");
        }

        // Show admin commands (only for admins)
        if (sender.hasPermission("poweredtrims.reload") || sender.hasPermission("poweredtrims.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/pt reload" + ChatColor.WHITE + " - Reload plugin configuration " + ChatColor.GRAY + "(Admin)");
        }

        if (sender.hasPermission("poweredtrims.debug") || sender.hasPermission("poweredtrims.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/pt debug" + ChatColor.WHITE + " - Show debug information " + ChatColor.GRAY + "(Admin)");
        }

        if (sender.hasPermission("poweredtrims.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/pt fixlore" + ChatColor.WHITE + " - Fix lore for all online players " + ChatColor.GRAY + "(Admin)");
        }

        sender.sendMessage(ChatColor.YELLOW + "/pt help" + ChatColor.WHITE + " - Show this help message");

        // Show permission info
        sender.sendMessage(ChatColor.GRAY + "Permissions:");
        sender.sendMessage(ChatColor.GRAY + "  poweredtrims.view/stats - View stats");
        sender.sendMessage(ChatColor.GRAY + "  poweredtrims.admin - All admin commands");
    }
    
    private String formatTrimName(String name) {
        return Arrays.stream(name.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce((a, b) -> a + " " + b)
                .orElse(name);
    }

    private String formatTrimKey(String trimKey) {
        // Format pattern_material to "Pattern (Material)"
        String[] parts = trimKey.split("_");
        if (parts.length >= 2) {
            String pattern = formatTrimName(parts[0]);
            String material = formatTrimName(parts[parts.length - 1]);
            return pattern + " (" + material + ")";
        }
        return formatTrimName(trimKey);
    }
    
    private String formatAttributeName(String name) {
        return name.replace("GENERIC_", "").replace("_", " ").toLowerCase();
    }
    
    private String formatEffectName(String name) {
        return Arrays.stream(name.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce((a, b) -> a + " " + b)
                .orElse(name);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();

            // Add stats if user has permission
            if (sender.hasPermission("poweredtrims.stats") || sender.hasPermission("poweredtrims.view")) {
                completions.add("stats");
            }

            // Add admin commands if user has permission
            if (sender.hasPermission("poweredtrims.reload") || sender.hasPermission("poweredtrims.admin")) {
                completions.add("reload");
            }

            if (sender.hasPermission("poweredtrims.debug") || sender.hasPermission("poweredtrims.admin")) {
                completions.add("debug");
            }

            if (sender.hasPermission("poweredtrims.admin")) {
                completions.add("fixlore");
            }

            // Help is always available
            completions.add("help");

            return completions;
        }

        return new ArrayList<>();
    }
}
