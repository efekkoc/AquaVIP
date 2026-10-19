package com.aquaforge.vip.commands;

import com.aquaforge.vip.AquaVipPlugin;
import com.aquaforge.vip.data.VipSubscription;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VipAdminCommand implements CommandExecutor, TabCompleter {

    private final AquaVipPlugin plugin;

    public VipAdminCommand(AquaVipPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("aquavip.admin")) {
            sender.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok.");
            return true;
        }

        if (label.equalsIgnoreCase("takevip")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Kullanım: /takevip <oyuncu>");
                return true;
            }
            String targetName = args[0];
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            boolean removed = plugin.getVipManager().takeVip(target.getUniqueId());
            if (removed) {
                sender.sendMessage(ChatColor.GREEN + targetName + " adlı oyuncunun VIP üyeliği silindi.");
            } else {
                sender.sendMessage(ChatColor.YELLOW + targetName + " adlı oyuncunun aktif VIP üyeliği bulunamadı.");
            }
            return true;
        }

        // /givevip <player> <tier> <duration>
        if (args.length < 3) {
            sender.sendMessage(ChatColor.YELLOW + "Kullanım: /givevip <oyuncu> <tier> <süre>");
            sender.sendMessage(ChatColor.GRAY + "Örnek: /givevip Steve VIP 30d");
            sender.sendMessage(ChatColor.GRAY + "Süre Tipleri: 30d, 7d, 24h, 60m, permanent");
            return true;
        }

        String targetName = args[0];
        String tier = args[1].toUpperCase();
        String durationStr = args[2].toLowerCase();

        if (!plugin.getConfig().contains("tiers." + tier)) {
            sender.sendMessage(ChatColor.RED + "Geçersiz VIP derecesi! Mevcut: " +
                    String.join(", ", plugin.getConfig().getConfigurationSection("tiers").getKeys(false)));
            return true;
        }

        long durationMillis = parseDuration(durationStr);
        if (durationMillis == -2) {
            sender.sendMessage(ChatColor.RED + "Geçersiz süre formatı! (Örn: 30d, 7d, 12h, permanent)");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        VipSubscription sub = plugin.getVipManager().giveVip(target.getUniqueId(), targetName, tier, durationMillis);

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            plugin.getBridge().syncRank(onlineTarget, tier);
        }

        sender.sendMessage(ChatColor.GREEN + "[AquaVIP] " + targetName + " adlı oyuncuya " + tier +
                " başarıyla tanımlandı! Kalan süre: " + sub.getRemainingFormatted());
        return true;
    }

    private long parseDuration(String input) {
        if (input.equalsIgnoreCase("permanent") || input.equalsIgnoreCase("perma") || input.equalsIgnoreCase("unlimited") || input.equals("-1")) {
            return -1;
        }
        try {
            if (input.endsWith("d")) {
                long days = Long.parseLong(input.substring(0, input.length() - 1));
                return TimeUnit.DAYS.toMillis(days);
            } else if (input.endsWith("h")) {
                long hours = Long.parseLong(input.substring(0, input.length() - 1));
                return TimeUnit.HOURS.toMillis(hours);
            } else if (input.endsWith("m")) {
                long minutes = Long.parseLong(input.substring(0, input.length() - 1));
                return TimeUnit.MINUTES.toMillis(minutes);
            } else if (input.endsWith("s")) {
                long seconds = Long.parseLong(input.substring(0, input.length() - 1));
                return TimeUnit.SECONDS.toMillis(seconds);
            } else {
                long days = Long.parseLong(input);
                return TimeUnit.DAYS.toMillis(days);
            }
        } catch (NumberFormatException e) {
            return -2;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("aquavip.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && alias.equalsIgnoreCase("givevip")) {
            if (plugin.getConfig().isConfigurationSection("tiers")) {
                return plugin.getConfig().getConfigurationSection("tiers").getKeys(false).stream()
                        .filter(t -> t.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && alias.equalsIgnoreCase("givevip")) {
            return Arrays.asList("30d", "15d", "7d", "24h", "permanent").stream()
                    .filter(d -> d.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
