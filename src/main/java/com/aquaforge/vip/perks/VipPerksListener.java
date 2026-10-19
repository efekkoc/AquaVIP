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

import java.util.ArrayList;
import java.util.List;

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

            // Spawn custom tier firework
            if (plugin.getConfig().getBoolean("settings.spawn-join-firework", true)) {
                spawnTierFirework(player.getLocation(), sub.getTier());
            }

            // Send notification
            String dName = plugin.getConfig().getString("tiers." + sub.getTier() + ".display-name", sub.getTier());
            player.sendMessage("§b§l[AquaVIP] §7Aktif üyeliğiniz: §f" + dName + " §8| §7Kalan Süre: §a" + sub.getRemainingFormatted());
        }
    }

    private void spawnTierFirework(Location loc, String tier) {
        try {
            Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
            FireworkMeta fwm = fw.getFireworkMeta();
            fwm.setPower(1);

            String path = "tiers." + tier + ".firework";
            String typeName = plugin.getConfig().getString(path + ".type", "BALL_LARGE");
            FireworkEffect.Type type;
            try {
                type = FireworkEffect.Type.valueOf(typeName.toUpperCase());
            } catch (Exception e) {
                type = FireworkEffect.Type.BALL_LARGE;
            }

            List<String> hexColors = plugin.getConfig().getStringList(path + ".colors");
            List<Color> colors = parseColors(hexColors, Color.AQUA);

            List<String> hexFades = plugin.getConfig().getStringList(path + ".fade");
            List<Color> fades = parseColors(hexFades, Color.WHITE);

            FireworkEffect effect = FireworkEffect.builder()
                    .with(type)
                    .withColor(colors)
                    .withFade(fades)
                    .trail(true)
                    .flicker(true)
                    .build();

            fwm.addEffect(effect);
            fw.setFireworkMeta(fwm);
        } catch (Exception ignored) {}
    }

    private List<Color> parseColors(List<String> hexList, Color fallback) {
        List<Color> list = new ArrayList<>();
        if (hexList == null || hexList.isEmpty()) {
            list.add(fallback);
            return list;
        }

        for (String hex : hexList) {
            try {
                java.awt.Color awt = java.awt.Color.decode(hex);
                list.add(Color.fromRGB(awt.getRed(), awt.getGreen(), awt.getBlue()));
            } catch (Exception e) {
                list.add(fallback);
            }
        }
        return list;
    }
}
