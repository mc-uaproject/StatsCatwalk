package dev.ua.ikeepcalm.statsCatwalk.config;

import dev.ua.ikeepcalm.statsCatwalk.StatsCatwalk;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class StatsConfig {
    
    private final int collectionIntervalMinutes;
    private final int dataRetentionDays;
    private final boolean enableTpsTracking;
    private final int maxTopPlayersLimit;
    private final boolean saveOnPlayerQuit;
    
    public StatsConfig(StatsCatwalk plugin) {
        FileConfiguration config = plugin.getConfig();
        
        this.collectionIntervalMinutes = config.getInt("collection.intervalMinutes", 10);
        this.dataRetentionDays = config.getInt("collection.dataRetentionDays", 14);
        this.enableTpsTracking = config.getBoolean("features.enableTpsTracking", true);
        this.maxTopPlayersLimit = config.getInt("features.maxTopPlayersLimit", 100);
        this.saveOnPlayerQuit = config.getBoolean("features.saveOnPlayerQuit", true);
    }
}