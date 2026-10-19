package com.aquaforge.vip.commands;

import com.aquaforge.vip.AquaVipPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VipPerksCommands implements CommandExecutor {

    private final AquaVipPlugin plugin;
    private final Map<UUID, Long> feedCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> healCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> nearCooldowns = new ConcurrentHashMap<>();

    public VipPerksCommands(AquaVipPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Bu komut yalnızca oyuncular tarafından kullanılabilir.");
            return true;
        }

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "fly":
                return handleFly(player);
            case "feed":
                return handleFeed(player);
            case "heal":
                return handleHeal(player);
            case "craft":
                return handleCraft(player);
            case "anvil":
                return handleAnvil(player);
            case "enderchest":
            case "ec":
                return handleEnderChest(player);
            case "hat":
                return handleHat(player);
            case "near":
                return handleNear(player);
            default:
                return false;
        }
    }

    private boolean handleFly(Player player) {
        if (!plugin.getVipManager().hasPerk(player, "fly") && !player.hasPermission("aquavip.perk.fly")) {
            player.sendMessage(ChatColor.RED + "Uçuş modu yetkiniz bulunmamaktadır! VIP satın alarak açabilirsiniz.");
            return true;
        }

        boolean flying = !player.getAllowFlight();
        player.setAllowFlight(flying);
        if (!flying) player.setFlying(false);

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&b&l[AquaVIP] &7Uçuş modu " + (flying ? "&a&lAKTİF" : "&c&lKAPALI") + "&7!"));
        return true;
    }

    private boolean handleFeed(Player player) {
        if (!plugin.getVipManager().hasPerk(player, "feed") && !player.hasPermission("aquavip.perk.feed")) {
            player.sendMessage(ChatColor.RED + "Açlık doldurma yetkiniz bulunmamaktadır.");
            return true;
        }

        long cooldown = plugin.getConfig().getLong("cooldowns.feed-seconds", 180) * 1000L;
        if (!player.hasPermission("aquavip.admin")) {
            Long last = feedCooldowns.get(player.getUniqueId());
            if (last != null && System.currentTimeMillis() - last < cooldown) {
                long remainingSec = (cooldown - (System.currentTimeMillis() - last)) / 1000L;
                player.sendMessage(ChatColor.RED + "Bu komutu tekrar kullanmak için " + remainingSec + " saniye beklemelisiniz.");
                return true;
            }
        }

        player.setFoodLevel(20);
        player.setSaturation(10f);
        feedCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "[AquaVIP] Açlığınız giderildi!");
        return true;
    }

    private boolean handleHeal(Player player) {
        if (!plugin.getVipManager().hasPerk(player, "heal") && !player.hasPermission("aquavip.perk.heal")) {
            player.sendMessage(ChatColor.RED + "Can yenileme yetkiniz bulunmamaktadır.");
            return true;
        }

        long cooldown = plugin.getConfig().getLong("cooldowns.heal-seconds", 600) * 1000L;
        if (!player.hasPermission("aquavip.admin")) {
            Long last = healCooldowns.get(player.getUniqueId());
            if (last != null && System.currentTimeMillis() - last < cooldown) {
                long remainingSec = (cooldown - (System.currentTimeMillis() - last)) / 1000L;
                player.sendMessage(ChatColor.RED + "Can yenilemek için " + remainingSec + " saniye beklemelisiniz.");
                return true;
            }
        }

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        healCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.sendMessage(ChatColor.GREEN + "[AquaVIP] Sağlığınız tamamen yenilendi!");
        return true;
    }

    private boolean handleCraft(Player player) {
        if (!plugin.getVipManager().hasPerk(player, "craft") && !player.hasPermission("aquavip.perk.craft")) {
            player.sendMessage(ChatColor.RED + "Sanal çalışma masası yetkiniz bulunmamaktadır.");
            return true;
        }
        player.openWorkbench(null, true);
        return true;
    }

    private boolean handleAnvil(Player player) {
        if (!plugin.getVipManager().hasPerk(player, "anvil") && !player.hasPermission("aquavip.perk.anvil")) {
            player.sendMessage(ChatColor.RED + "Sanal örs yetkiniz bulunmamaktadır.");
            return true;
        }
        player.openAnvil(null, true);
        return true;
    }

    private boolean handleEnderChest(Player player) {
        if (!plugin.getVipManager().hasPerk(player, "enderchest") && !player.hasPermission("aquavip.perk.enderchest")) {
            player.sendMessage(ChatColor.RED + "Sanal ender sandığı yetkiniz bulunmamaktadır.");
            return true;
        }
        player.openInventory(player.getEnderChest());
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
        return true;
    }

    private boolean handleHat(Player player) {
        if (!plugin.getVipManager().hasPerk(player, "hat") && !player.hasPermission("aquavip.perk.hat")) {
            player.sendMessage(ChatColor.RED + "Şapka takma yetkiniz bulunmamaktadır.");
            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "Lütfen kafanıza takmak istediğiniz eşyayı elinizde tutun.");
            return true;
        }

        ItemStack helmet = player.getInventory().getHelmet();
        player.getInventory().setHelmet(hand.clone());
        player.getInventory().setItemInMainHand(helmet);

        player.sendMessage(ChatColor.GREEN + "[AquaVIP] Seçilen eşya kafanıza şapka olarak takıldı!");
        return true;
    }

    private boolean handleNear(Player player) {
        if (!plugin.getVipManager().hasPerk(player, "near") && !player.hasPermission("aquavip.perk.near")) {
            player.sendMessage(ChatColor.RED + "Yakındaki oyuncuları tarama yetkiniz bulunmamaktadır.");
            return true;
        }

        long cooldown = plugin.getConfig().getLong("cooldowns.near-seconds", 30) * 1000L;
        if (!player.hasPermission("aquavip.admin")) {
            Long last = nearCooldowns.get(player.getUniqueId());
            if (last != null && System.currentTimeMillis() - last < cooldown) {
                long remainingSec = (cooldown - (System.currentTimeMillis() - last)) / 1000L;
                player.sendMessage(ChatColor.RED + "Tekrar taramak için " + remainingSec + " saniye bekleyin.");
                return true;
            }
        }

        double radius = 50.0;
        var sub = plugin.getVipManager().getSubscription(player.getUniqueId());
        if (sub != null) {
            radius = plugin.getConfig().getDouble("tiers." + sub.getTier() + ".near-radius", 50.0);
        }

        List<Player> nearby = player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius).stream()
                .filter(e -> e instanceof Player && !e.getUniqueId().equals(player.getUniqueId()))
                .map(e -> (Player) e)
                .collect(Collectors.toList());

        nearCooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        if (nearby.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "[AquaVIP] " + (int) radius + " blok yakınınızda oyuncu bulunamadı.");
        } else {
            player.sendMessage(ChatColor.AQUA + "--- Yakındaki Oyuncular (" + (int) radius + " Blok) ---");
            for (Player p : nearby) {
                int dist = (int) player.getLocation().distance(p.getLocation());
                player.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + p.getName() + ChatColor.GRAY + " (" + dist + "m uzaklıkta)");
            }
        }
        return true;
    }
}
