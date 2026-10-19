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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VipInfoCommand implements CommandExecutor, Listener {

    private final AquaVipPlugin plugin;
    public static final String GUI_TITLE = "§8✦ §b§lSERVER RANKS §8✦";

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
            if (sender instanceof Player player) {
                openVipGui(player);
                return true;
            } else {
                return handleVipConsole(sender);
            }
        }
    }

    public void openVipGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Fill background with dark gray glass
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, filler);
        }

        VipSubscription sub = plugin.getVipManager().getSubscription(player.getUniqueId());

        // 1. VIP (Slot 11) - Lime
        gui.setItem(11, buildRankIcon("VIP", Material.LIME_STAINED_GLASS, sub));

        // 2. VIP+ (Slot 12) - Emerald Green
        gui.setItem(12, buildRankIcon("VIP_PLUS", Material.GREEN_STAINED_GLASS, sub));

        // 3. MVP (Slot 13) - Diamond / Light Blue
        gui.setItem(13, buildRankIcon("MVP", Material.LIGHT_BLUE_STAINED_GLASS, sub));

        // 4. MVP+ (Slot 14) - Gold / Yellow
        gui.setItem(14, buildRankIcon("MVP_PLUS", Material.YELLOW_STAINED_GLASS, sub));

        // 5. ELITE (Slot 15) - Purple / Obsidian
        gui.setItem(15, buildRankIcon("ELITE", Material.PURPLE_STAINED_GLASS, sub));

        // Status Book (Slot 22)
        ItemStack status = new ItemStack(Material.NETHER_STAR);
        ItemMeta sm = status.getItemMeta();
        sm.setDisplayName("§e§lÜYELİK DURUMUNUZ");
        List<String> statusLore = new ArrayList<>();
        if (sub != null) {
            String glyph = plugin.getConfig().getString("tiers." + sub.getTier() + ".glyph", "");
            statusLore.add("§7Aktif Rütbeniz: " + glyph + " " + plugin.getConfig().getString("tiers." + sub.getTier() + ".display-name", sub.getTier()));
            statusLore.add("§7Kalan Süre: §a" + sub.getRemainingFormatted());
        } else {
            statusLore.add("§7Aktif Rütbeniz: §cBulunmuyor");
            statusLore.add("§eMağazadan satın alarak ayrıcalıklara kavuşabilirsiniz!");
        }
        sm.setLore(statusLore);
        status.setItemMeta(sm);
        gui.setItem(22, status);

        player.openInventory(gui);
    }

    private ItemStack buildRankIcon(String tierKey, Material mat, VipSubscription userSub) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        String dName = plugin.getConfig().getString("tiers." + tierKey + ".display-name", tierKey);
        String glyph = plugin.getConfig().getString("tiers." + tierKey + ".glyph", "");
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', glyph + " " + dName));

        List<String> lore = new ArrayList<>();
        lore.add("§8────────────────────");
        lore.add("§eAyrıcalıklar:");
        List<String> perks = plugin.getConfig().getStringList("tiers." + tierKey + ".perks");
        for (String p : perks) {
            lore.add(" §8▪ §f/" + p);
        }

        lore.add("§8────────────────────");
        if (userSub != null && userSub.getTier().equalsIgnoreCase(tierKey)) {
            lore.add("§a✔ Bu rütbeye sahipsiniz!");
            lore.add("§7Kalan: §e" + userSub.getRemainingFormatted());
            lore.add("§6[Tıkla] §eKitini teslim al!");
        } else {
            lore.add("§7Durum: §cKilitli");
            lore.add("§eWeb mağazasından erişilebilir.");
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();

        String tierToClaim = null;
        if (slot == 11) tierToClaim = "VIP";
        else if (slot == 12) tierToClaim = "VIP_PLUS";
        else if (slot == 13) tierToClaim = "MVP";
        else if (slot == 14) tierToClaim = "MVP_PLUS";
        else if (slot == 15) tierToClaim = "ELITE";

        if (tierToClaim != null) {
            handleVipKit(player, new String[]{tierToClaim});
        }
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private boolean handleVipTime(CommandSender sender, String[] args) {
        String targetName = (args.length > 0) ? args[0] : sender.getName();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        VipSubscription sub = plugin.getVipManager().getSubscription(target.getUniqueId());
        if (sub == null) {
            sender.sendMessage(ChatColor.YELLOW + "[AquaVIP] " + targetName + " adlı kullanıcının aktif bir VIP aboneliği yok.");
            return true;
        }

        String glyph = plugin.getConfig().getString("tiers." + sub.getTier() + ".glyph", "★");
        sender.sendMessage(ChatColor.AQUA + "========================================");
        sender.sendMessage(ChatColor.GOLD + "  " + glyph + " " + sub.getPlayerName() + " - VIP Durumu");
        sender.sendMessage(ChatColor.GRAY + "  Üyelik Derecesi: " + ChatColor.YELLOW + sub.getTier());
        sender.sendMessage(ChatColor.GRAY + "  Kalan Süre: " + ChatColor.GREEN + sub.getRemainingFormatted());
        sender.sendMessage(ChatColor.AQUA + "========================================");
        return true;
    }

    private boolean handleVipConsole(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_AQUA + "================== " + ChatColor.AQUA + "SERVER RANKS" + ChatColor.DARK_AQUA + " ==================");
        if (plugin.getConfig().isConfigurationSection("tiers")) {
            for (String key : plugin.getConfig().getConfigurationSection("tiers").getKeys(false)) {
                String dName = plugin.getConfig().getString("tiers." + key + ".display-name", key);
                String glyph = plugin.getConfig().getString("tiers." + key + ".glyph", "");
                List<String> perks = plugin.getConfig().getStringList("tiers." + key + ".perks");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        " " + glyph + " " + dName + " &8» &f" + String.join("&7, &f", perks)));
            }
        }
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
