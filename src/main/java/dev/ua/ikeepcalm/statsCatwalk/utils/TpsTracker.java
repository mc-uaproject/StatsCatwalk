package dev.ua.ikeepcalm.statsCatwalk.utils;

import dev.ua.ikeepcalm.statsCatwalk.StatsCatwalk;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class TpsTracker {
    private final StatsCatwalk plugin;
    private BukkitTask trackingTask;
    private Spark spark;
    private double currentTPS = 20.0;

    public TpsTracker(StatsCatwalk plugin) {
        this.plugin = plugin;
        startTracking();
    }

    private void startTracking() {
        trackingTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateTPS, 100L, 20L);
    }

    public void stop() {
        if (trackingTask != null) {
            trackingTask.cancel();
        }
    }

    private void updateTPS() {
        try {
            if (spark == null) {
                spark = SparkProvider.get();
            }
            
            DoubleStatistic<StatisticWindow.TicksPerSecond> tps = spark.tps();
            if (tps != null) {
                currentTPS = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_1);
            }
        } catch (IllegalStateException e) {
            // Spark is not available, keep current TPS value
        }
    }

    public double getTPS() {
        return currentTPS;
    }

    public String getTPSString() {
        return String.format("%.2f", getTPS());
    }
}