package com.aquaforge.vip.data;

import com.aquaforge.vip.AquaVipPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VipManager {

    private final AquaVipPlugin plugin;
    private final Map<UUID, VipSubscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, Long> kitCooldowns = new ConcurrentHashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public VipManager(AquaVipPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "vip_data.yml");
        loadData();
    }

    public void loadData() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create vip_data.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        subscriptions.clear();

        ConfigurationSection vipsSection = dataConfig.getConfigurationSection("subscriptions");
        if (vipsSection != null) {
            for (String key : vipsSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String name = vipsSection.getString(key + ".name", "Unknown");
                    String tier = vipsSection.getString(key + ".tier", "VIP");
                    long granted = vipsSection.getLong(key + ".granted", System.currentTimeMillis());
                    long expire = vipsSection.getLong(key + ".expire", -1);
                    boolean active = vipsSection.getBoolean(key + ".active", true);

                    VipSubscription sub = new VipSubscription(uuid, name, tier, granted, expire);
                    sub.setActive(active);
                    subscriptions.put(uuid, sub);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        ConfigurationSection kitsSection = dataConfig.getConfigurationSection("kit_cooldowns");
        if (kitsSection != null) {
            for (String key : kitsSection.getKeys(false)) {
                kitCooldowns.put(key, kitsSection.getLong(key));
            }
        }
    }

    public void saveData() {
        if (dataConfig == null) dataConfig = new YamlConfiguration();
        dataConfig.set("subscriptions", null);

        for (Map.Entry<UUID, VipSubscription> entry : subscriptions.entrySet()) {
            VipSubscription sub = entry.getValue();
            String path = "subscriptions." + entry.getKey().toString();
            dataConfig.set(path + ".name", sub.getPlayerName());
            dataConfig.set(path + ".tier", sub.getTier());
            dataConfig.set(path + ".granted", sub.getGrantedTimeMillis());
            dataConfig.set(path + ".expire", sub.getExpirationTimeMillis());
            dataConfig.set(path + ".active", sub.isActive());
        }

        dataConfig.set("kit_cooldowns", null);
        for (Map.Entry<String, Long> entry : kitCooldowns.entrySet()) {
            dataConfig.set("kit_cooldowns." + entry.getKey(), entry.getValue());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save vip_data.yml: " + e.getMessage());
        }
    }

    public VipSubscription getSubscription(UUID uuid) {
        VipSubscription sub = subscriptions.get(uuid);
        if (sub != null && sub.isActive() && !sub.isExpired()) {
            return sub;
        }
        return null;
    }

    public VipSubscription getAnySubscription(UUID uuid) {
        return subscriptions.get(uuid);
    }

    public VipSubscription giveVip(UUID uuid, String playerName, String tier, long durationMillis) {
        long now = System.currentTimeMillis();
        long expire = (durationMillis <= 0) ? -1 : (now + durationMillis);

        VipSubscription existing = subscriptions.get(uuid);
        if (existing != null && existing.isActive() && !existing.isExpired() && existing.getTier().equalsIgnoreCase(tier)) {
            // Extend existing subscription
            if (existing.isPermanent() || durationMillis <= 0) {
                existing.setExpirationTimeMillis(-1);
            } else {
                long currentExpire = Math.max(existing.getExpirationTimeMillis(), now);
                existing.setExpirationTimeMillis(currentExpire + durationMillis);
            }
            existing.setPlayerName(playerName);
            saveData();
            return existing;
        }

        VipSubscription sub = new VipSubscription(uuid, playerName, tier.toUpperCase(), now, expire);
        subscriptions.put(uuid, sub);
        saveData();

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&a&l[AquaVIP] &eTebrikler! Hesabınıza &b" + tier.toUpperCase() + " &eüyeliği tanımlandı!"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&a&l[AquaVIP] &7Kalan Süre: &f" + sub.getRemainingFormatted()));
        }

        return sub;
    }

    public boolean takeVip(UUID uuid) {
        VipSubscription sub = subscriptions.get(uuid);
        if (sub != null) {
            sub.setActive(false);
            saveData();

            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                if (player.getAllowFlight() && !player.hasPermission("aquavip.admin")) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&c&l[AquaVIP] &7VIP aboneliğiniz bir yönetici tarafından sonlandırıldı."));
            }
            return true;
        }
        return false;
    }

    public boolean hasPerk(Player player, String perkName) {
        if (player.hasPermission("aquavip.admin") || player.isOp()) return true;
        VipSubscription sub = getSubscription(player.getUniqueId());
        if (sub == null) return false;

        String tierKey = sub.getTier().toUpperCase();
        List<String> perks = plugin.getConfig().getStringList("tiers." + tierKey + ".perks");
        return perks.stream().anyMatch(p -> p.equalsIgnoreCase(perkName));
    }

    public boolean canClaimKit(UUID uuid, String tier) {
        String key = uuid.toString() + ":" + tier.toLowerCase();
        Long nextClaim = kitCooldowns.get(key);
        return nextClaim == null || System.currentTimeMillis() >= nextClaim;
    }

    public long getKitCooldownRemainingMillis(UUID uuid, String tier) {
        String key = uuid.toString() + ":" + tier.toLowerCase();
        Long nextClaim = kitCooldowns.get(key);
        if (nextClaim == null) return 0;
        return Math.max(0, nextClaim - System.currentTimeMillis());
    }

    public void setKitClaimed(UUID uuid, String tier, int hours) {
        String key = uuid.toString() + ":" + tier.toLowerCase();
        long next = System.currentTimeMillis() + (hours * 3600000L);
        kitCooldowns.put(key, next);
        saveData();
    }

    public void checkExpirations() {
        long now = System.currentTimeMillis();
        boolean changed = false;

        for (Map.Entry<UUID, VipSubscription> entry : subscriptions.entrySet()) {
            VipSubscription sub = entry.getValue();
            if (sub.isActive() && !sub.isPermanent() && now >= sub.getExpirationTimeMillis()) {
                sub.setActive(false);
                changed = true;

                Player player = Bukkit.getPlayer(sub.getUuid());
                if (player != null && player.isOnline()) {
                    if (player.getAllowFlight() && !player.hasPermission("aquavip.admin")) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    }
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&c&l[AquaVIP] &e" + sub.getTier() + " &7abonelik süreniz doldu! Yenilemek için mağazamızı ziyaret edebilirsiniz."));
                }
                plugin.getLogger().info("Expired VIP subscription for: " + sub.getPlayerName() + " (" + sub.getTier() + ")");
            }
        }

        if (changed) {
            saveData();
        }
    }
}
