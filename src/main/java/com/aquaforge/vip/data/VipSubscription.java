package com.aquaforge.vip.data;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VipSubscription {
    private final UUID uuid;
    private String playerName;
    private String tier;
    private final long grantedTimeMillis;
    private long expirationTimeMillis; // -1 = permanent
    private boolean active;

    public VipSubscription(UUID uuid, String playerName, String tier, long grantedTimeMillis, long expirationTimeMillis) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.tier = tier;
        this.grantedTimeMillis = grantedTimeMillis;
        this.expirationTimeMillis = expirationTimeMillis;
        this.active = true;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public long getGrantedTimeMillis() {
        return grantedTimeMillis;
    }

    public long getExpirationTimeMillis() {
        return expirationTimeMillis;
    }

    public void setExpirationTimeMillis(long expirationTimeMillis) {
        this.expirationTimeMillis = expirationTimeMillis;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPermanent() {
        return expirationTimeMillis <= 0;
    }

    public boolean isExpired() {
        if (!active) return true;
        if (isPermanent()) return false;
        return System.currentTimeMillis() >= expirationTimeMillis;
    }

    public String getRemainingFormatted() {
        if (!active) return "§cİnaktif";
        if (isPermanent()) return "§aSınırsız (Kalıcı)";
        long diff = expirationTimeMillis - System.currentTimeMillis();
        if (diff <= 0) return "§cSüresi Doldu";

        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" gün ");
        if (hours > 0 || days > 0) sb.append(hours).append(" saat ");
        if (minutes > 0 || hours > 0 || days > 0) sb.append(minutes).append(" dk ");
        sb.append(seconds).append(" sn");

        return sb.toString().trim();
    }
}
