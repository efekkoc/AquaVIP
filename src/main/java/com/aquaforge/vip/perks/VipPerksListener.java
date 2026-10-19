package com.aquaforge.vip.perks;

import com.aquaforge.vip.AquaVipPlugin;
import com.aquaforge.vip.data.VipSubscription;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public class VipPerksListener implements Listener {

    private final AquaVipPlugin plugin;

    public VipPerksListener(AquaVipPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!plugin.getConfig().getBoolean("settings.enable-priority-join", true)) return;

        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            Player player = event.getPlayer();
            VipSubscription sub = plugin.getVipManager().getSubscription(player.getUniqueId());
            boolean hasBypassPerm = player.hasPermission("aquavip.fullserver.bypass") || player.isOp();

            if (sub != null || hasBypassPerm) {
                event.allow();
                plugin.getLogger().info("Priority Join granted to VIP player: " + player.getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        VipSubscription sub = plugin.getVipManager().getSubscription(player.getUniqueId());

        if (sub != null) {
            // Keep player name up to date
            sub.setPlayerName(player.getName());

            // Broadcast join message
            if (plugin.getConfig().getBoolean("settings.broadcast-vip-join", true)) {
                plugin.getBridge().broadcastVipJoin(player, sub.getTier());
            }

            // Spawn firework
            if (plugin.getConfig().getBoolean("settings.spawn-join-firework", true)) {
                spawnFirework(player.getLocation());
            }

            // Send notification
            player.sendMessage("§b§l[AquaVIP] §7Aktif üyeliğiniz: §e" + sub.getTier() + " §8| §7Kalan Süre: §a" + sub.getRemainingFormatted());
        }
    }

    private void spawnFirework(Location loc) {
        try {
            Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
            FireworkMeta fwm = fw.getFireworkMeta();
            fwm.setPower(1);
            fwm.addEffect(FireworkEffect.builder()
                    .withColor(Color.AQUA, Color.TEAL, Color.WHITE)
                    .withFade(Color.NAVY)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .trail(true)
                    .build());
            fw.setFireworkMeta(fwm);
        } catch (Exception ignored) {}
    }
}
