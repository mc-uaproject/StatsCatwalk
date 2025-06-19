package dev.ua.ikeepcalm.statsCatwalk.utils;

import dev.ua.ikeepcalm.statsCatwalk.StatsCatwalk;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class TpsTracker {
    private final List<Double> tpsHistory = new ArrayList<>();
    private final StatsCatwalk plugin;
    private BukkitTask trackingTask;
    private long lastPoll = System.nanoTime();

    public TpsTracker(StatsCatwalk plugin) {
        this.plugin = plugin;
        startTracking();
    }

    private void startTracking() {
        trackingTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 100L, 1L);
    }

    public void stop() {
        if (trackingTask != null) {
            trackingTask.cancel();
        }
    }

    private void tick() {
        long now = System.nanoTime();
        long timeSpent = (now - lastPoll) / 1000;

        if (timeSpent == 0) timeSpent = 1;

        if (tpsHistory.size() > 10) {
            tpsHistory.remove(0);
        }

        double tps = 20000000.0 / timeSpent;
        if (tps <= 21) {
            tpsHistory.add(tps);
        }

        lastPoll = now;
    }

    public double getTPS() {
        if (tpsHistory.isEmpty()) {
            return 20.0;
        }

        double sum = 0;
        for (double tps : tpsHistory) {
            sum += tps;
        }
        return sum / tpsHistory.size();
    }

    public String getTPSString() {
        return String.format("%.2f", getTPS());
    }
}