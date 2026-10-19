package com.aquaforge.vip.core;

import com.aquaforge.vip.AquaVipPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class AquaForgeBridge {

    private final AquaVipPlugin plugin;
    private boolean aquaRanksPresent = false;
    private boolean aquaChatPresent = false;
    private boolean aquaEconomyPresent = false;
    private boolean aquaScoreboardPresent = false;

    public AquaForgeBridge(AquaVipPlugin plugin) {
        this.plugin = plugin;
        detectEcosystem();
    }

    public void detectEcosystem() {
        Plugin ranks = Bukkit.getPluginManager().getPlugin("AquaRanks");
        aquaRanksPresent = (ranks != null && ranks.isEnabled());

        Plugin chat = Bukkit.getPluginManager().getPlugin("AquaChat");
        aquaChatPresent = (chat != null && chat.isEnabled());

        Plugin eco = Bukkit.getPluginManager().getPlugin("AquaEconomy");
        aquaEconomyPresent = (eco != null && eco.isEnabled());

        Plugin sb = Bukkit.getPluginManager().getPlugin("AquaScoreboard");
        aquaScoreboardPresent = (sb != null && sb.isEnabled());

        plugin.getLogger().info("AquaForge Synergy Connected: [Ranks: " + aquaRanksPresent
                + ", Chat: " + aquaChatPresent
                + ", Economy: " + aquaEconomyPresent
                + ", Scoreboard: " + aquaScoreboardPresent + "]");
    }

    public void syncRank(Player player, String tier) {
        if (!aquaRanksPresent) return;
        try {
            // Can execute console command to sync rank seamlessly
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "setrank " + player.getName() + " " + tier);
        } catch (Exception ignored) {}
    }

    public void broadcastVipJoin(Player player, String tier) {
        String joinMsg = plugin.getConfig().getString("tiers." + tier + ".join-message");
        if (joinMsg != null && !joinMsg.isEmpty()) {
            String formatted = ChatColor.translateAlternateColorCodes('&', joinMsg.replace("{player}", player.getName()));
            Bukkit.broadcastMessage(formatted);
        }
    }
}
