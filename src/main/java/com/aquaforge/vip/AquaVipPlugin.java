package com.aquaforge.vip;

import com.aquaforge.vip.commands.VipAdminCommand;
import com.aquaforge.vip.commands.VipInfoCommand;
import com.aquaforge.vip.commands.VipPerksCommands;
import com.aquaforge.vip.core.AquaForgeBridge;
import com.aquaforge.vip.data.VipManager;
import com.aquaforge.vip.perks.VipPerksListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class AquaVipPlugin extends JavaPlugin {

    private static AquaVipPlugin instance;
    private VipManager vipManager;
    private AquaForgeBridge bridge;
    private BukkitTask expirationTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        printBanner();

        this.vipManager = new VipManager(this);
        this.bridge = new AquaForgeBridge(this);

        // Register event listener
        getServer().getPluginManager().registerEvents(new VipPerksListener(this), this);

        // Register Admin Commands
        VipAdminCommand adminCmd = new VipAdminCommand(this);
        getCommand("givevip").setExecutor(adminCmd);
        getCommand("givevip").setTabCompleter(adminCmd);
        getCommand("takevip").setExecutor(adminCmd);
        getCommand("takevip").setTabCompleter(adminCmd);

        // Register Info & Kit Commands
        VipInfoCommand infoCmd = new VipInfoCommand(this);
        getCommand("vip").setExecutor(infoCmd);
        getCommand("viptime").setExecutor(infoCmd);
        getCommand("vipkit").setExecutor(infoCmd);

        // Register Perks Commands
        VipPerksCommands perksCmd = new VipPerksCommands(this);
        getCommand("fly").setExecutor(perksCmd);
        getCommand("feed").setExecutor(perksCmd);
        getCommand("heal").setExecutor(perksCmd);
        getCommand("craft").setExecutor(perksCmd);
        getCommand("anvil").setExecutor(perksCmd);
        getCommand("enderchest").setExecutor(perksCmd);
        getCommand("hat").setExecutor(perksCmd);
        getCommand("near").setExecutor(perksCmd);

        // Schedule expiration checker
        int intervalSeconds = getConfig().getInt("settings.check-interval-seconds", 60);
        long ticks = Math.max(20L, intervalSeconds * 20L);

        expirationTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            vipManager.checkExpirations();
        }, ticks, ticks);

        getLogger().info("AquaVIP v1.0.0 successfully loaded! Monetization & perks engine online.");
    }

    @Override
    public void onDisable() {
        if (expirationTask != null && !expirationTask.isCancelled()) {
            expirationTask.cancel();
        }
        if (vipManager != null) {
            vipManager.saveData();
        }
        getLogger().info("AquaVIP safely disabled. All active subscriptions persisted.");
    }

    private void printBanner() {
        getLogger().info("=================================================");
        getLogger().info("   ___                     _   _ ___ ___  ");
        getLogger().info("  / _ \\  __ _ _   _  __ _ | | | |_ _| _ \\ ");
        getLogger().info(" / /_\\ \\/ _` | | | |/ _` || | | || ||  _/ ");
        getLogger().info("/ /_\\\\ \\ (_| | |_| | (_| | \\ V / | || |   ");
        getLogger().info("\\____/\\_\\__, |\\__,_|\\__,_|  \\_/ |___|_|   ");
        getLogger().info("           |_|                           ");
        getLogger().info("  AquaForge Ecosystem - Multi-Tier Monetization");
        getLogger().info("  Developed by: Turgut Efekan Koç (@efekkoc)");
        getLogger().info("=================================================");
    }

    public static AquaVipPlugin getInstance() {
        return instance;
    }

    public VipManager getVipManager() {
        return vipManager;
    }

    public AquaForgeBridge getBridge() {
        return bridge;
    }
}
