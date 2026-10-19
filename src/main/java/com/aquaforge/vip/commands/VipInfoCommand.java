package com.aquaforge.vip.commands;

import com.aquaforge.vip.AquaVipPlugin;
import com.aquaforge.vip.data.VipSubscription;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class VipInfoCommand implements CommandExecutor {

    private final AquaVipPlugin plugin;

    public VipInfoCommand(AquaVipPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("viptime")) {
            return handleVipTime(sender, args);
        } else if (cmd.equals("vipkit")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Bu komut yalnızca oyuncular içindir.");
                return true;
            }
            return handleVipKit(player, args);
        } else {
            return handleVipMenu(sender);
        }
    }

    private boolean handleVipTime(CommandSender sender, String[] args) {
        String targetName = (args.length > 0) ? args[0] : sender.getName();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        VipSubscription sub = plugin.getVipManager().getSubscription(target.getUniqueId());
        if (sub == null) {
            sender.sendMessage(ChatColor.YELLOW + "[AquaVIP] " + targetName + " adlı kullanıcının aktif bir VIP aboneliği yok.");
            return true;
        }

        sender.sendMessage(ChatColor.AQUA + "========================================");
        sender.sendMessage(ChatColor.GOLD + "  ★ " + sub.getPlayerName() + " - VIP Durumu");
        sender.sendMessage(ChatColor.GRAY + "  Üyelik Derecesi: " + ChatColor.YELLOW + sub.getTier());
        sender.sendMessage(ChatColor.GRAY + "  Kalan Süre: " + ChatColor.GREEN + sub.getRemainingFormatted());
        sender.sendMessage(ChatColor.AQUA + "========================================");
        return true;
    }

    private boolean handleVipMenu(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_AQUA + "================== " + ChatColor.AQUA + "AQUA FORGE VIP" + ChatColor.DARK_AQUA + " ==================");
        if (sender instanceof Player player) {
            VipSubscription sub = plugin.getVipManager().getSubscription(player.getUniqueId());
            if (sub != null) {
                sender.sendMessage(ChatColor.GREEN + "Mevcut Üyeliğiniz: " + ChatColor.GOLD + sub.getTier() +
                        ChatColor.GRAY + " (Kalan: " + ChatColor.YELLOW + sub.getRemainingFormatted() + ChatColor.GRAY + ")");
            } else {
                sender.sendMessage(ChatColor.GRAY + "Mevcut Üyeliğiniz: " + ChatColor.RED + "Aktif VIP Bulunmuyor");
            }
        }

        sender.sendMessage(ChatColor.YELLOW + "Mevcut VIP Paketleri & Ayrıcalıkları:");
        if (plugin.getConfig().isConfigurationSection("tiers")) {
            for (String key : plugin.getConfig().getConfigurationSection("tiers").getKeys(false)) {
                String dName = plugin.getConfig().getString("tiers." + key + ".display-name", key);
                List<String> perks = plugin.getConfig().getStringList("tiers." + key + ".perks");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        " " + dName + " &8» &f" + String.join(", ", perks)));
            }
        }
        sender.sendMessage(ChatColor.GRAY + "VIP satın almak ve sunucuyu desteklemek için web mağazamızı ziyaret edin!");
        sender.sendMessage(ChatColor.DARK_AQUA + "==================================================");
        return true;
    }

    private boolean handleVipKit(Player player, String[] args) {
        VipSubscription sub = plugin.getVipManager().getSubscription(player.getUniqueId());
        if (sub == null && !player.hasPermission("aquavip.admin")) {
            player.sendMessage(ChatColor.RED + "VIP kitlerini yalnızca aktif VIP üyeleri alabilir!");
            return true;
        }

        String tier = (args.length > 0) ? args[0].toUpperCase() : (sub != null ? sub.getTier() : "VIP");
        String path = "tiers." + tier + ".kit";

        if (!plugin.getConfig().contains(path)) {
            player.sendMessage(ChatColor.RED + "Bu seviye için tanımlı kit bulunamadı!");
            return true;
        }

        int cooldownHours = plugin.getConfig().getInt(path + ".cooldown-hours", 24);
        if (!player.hasPermission("aquavip.admin") && !plugin.getVipManager().canClaimKit(player.getUniqueId(), tier)) {
            long remaining = plugin.getVipManager().getKitCooldownRemainingMillis(player.getUniqueId(), tier);
            long hours = TimeUnit.MILLISECONDS.toHours(remaining);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60;
            player.sendMessage(ChatColor.RED + "Bu kiti tekrar almak için " + hours + " saat " + minutes + " dakika beklemelisiniz.");
            return true;
        }

        List<String> itemStrings = plugin.getConfig().getStringList(path + ".items");
        for (String raw : itemStrings) {
            ItemStack item = parseItem(raw);
            if (item != null) {
                player.getInventory().addItem(item);
            }
        }

        plugin.getVipManager().setKitClaimed(player.getUniqueId(), tier, cooldownHours);
        player.sendMessage(ChatColor.GREEN + "[AquaVIP] " + tier + " kitiniz envanterinize teslim edildi!");
        return true;
    }

    private ItemStack parseItem(String raw) {
        // format: MATERIAL:AMOUNT:ENCHANT:LEVEL:ENCHANT2:LEVEL
        String[] parts = raw.split(":");
        try {
            Material mat = Material.matchMaterial(parts[0]);
            if (mat == null) return null;
            int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
            ItemStack stack = new ItemStack(mat, amount);

            if (parts.length > 3) {
                for (int i = 2; i < parts.length; i += 2) {
                    if (i + 1 < parts.length) {
                        String enchName = parts[i].toUpperCase();
                        int lvl = Integer.parseInt(parts[i + 1]);
                        Enchantment ench = getEnchantment(enchName);
                        if (ench != null) {
                            stack.addUnsafeEnchantment(ench, lvl);
                        }
                    }
                }
            }
            return stack;
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private Enchantment getEnchantment(String name) {
        // Modern Paper enchantment lookup
        Enchantment ench = Enchantment.getByName(name);
        if (ench == null) {
            try {
                org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(name.toLowerCase());
                ench = org.bukkit.Registry.ENCHANTMENT.get(key);
            } catch (Exception ignored) {}
        }
        return ench;
    }
}
