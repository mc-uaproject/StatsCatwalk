package dev.ua.ikeepcalm.statsCatwalk.manager;

import dev.ua.ikeepcalm.statsCatwalk.StatsCatwalk;
import dev.ua.ikeepcalm.statsCatwalk.api.response.OnlinePlayerData;
import dev.ua.ikeepcalm.statsCatwalk.api.response.TopPlayerData;
import dev.ua.ikeepcalm.statsCatwalk.config.StatsConfig;
import dev.ua.ikeepcalm.statsCatwalk.utils.TpsTracker;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StatsManager {
    private final StatsCatwalk plugin;
    private final Logger logger;
    private final Path dataFolder;
    private final ZoneId serverTimeZone;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final StatsConfig config;
    private final TpsTracker tpsTracker;

    private final Map<Long, Integer> onlinePlayerHistory = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> hourlyDistribution = new ConcurrentHashMap<>();
    private final Map<UUID, Long> playerPlaytimes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> playerSessions = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerLevels = new ConcurrentHashMap<>();

    private BukkitTask collectionTask;

    public StatsManager(StatsCatwalk plugin, StatsConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.logger = plugin.getLogger();
        this.dataFolder = new File(plugin.getDataFolder(), "stats").toPath();
        this.serverTimeZone = ZoneId.systemDefault();
        this.tpsTracker = new TpsTracker(plugin);

        try {
            Files.createDirectories(dataFolder);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create stats directory", e);
        }

        loadData();
        startCollectionTask();
    }

    public void startCollectionTask() {
        if (collectionTask != null) {
            collectionTask.cancel();
        }

        long intervalTicks = 20L * 60L * config.getCollectionIntervalMinutes();
        collectionTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::collectAndSaveData, 20L, intervalTicks);
    }

    public void stop() {
        if (collectionTask != null) {
            collectionTask.cancel();
            collectionTask = null;
        }
        tpsTracker.stop();
        saveData();
    }

    public void handlePlayerJoin(UUID playerUuid) {
        playerSessions.put(playerUuid, System.currentTimeMillis());
    }

    public void handlePlayerQuit(UUID playerUuid) {
        long sessionStart = playerSessions.getOrDefault(playerUuid, System.currentTimeMillis());
        long sessionDuration = System.currentTimeMillis() - sessionStart;

        playerPlaytimes.put(playerUuid, playerPlaytimes.getOrDefault(playerUuid, 0L) + sessionDuration);
        playerSessions.remove(playerUuid);

        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
            playerLevels.put(playerUuid, player.getLevel());
        }

        if (config.isSaveOnPlayerQuit()) {
            savePlayerPlaytimes();
            savePlayerLevels();
        }
    }

    private void collectAndSaveData() {
        long timestamp = System.currentTimeMillis();
        int onlineCount = Bukkit.getOnlinePlayers().size();

        onlinePlayerHistory.put(timestamp, onlineCount);

        LocalDateTime now = LocalDateTime.now(serverTimeZone);
        String hour = now.format(timeFormatter);
        String date = now.format(dateFormatter);

        if (now.getMinute() == 0) {
            return;
        }

        hourlyDistribution.computeIfAbsent(hour, k -> new HashMap<>()).put(date, onlineCount);

        saveOnlinePlayerHistory();
        saveHourlyDistribution();
    }

    private void loadData() {
        loadOnlinePlayerHistory();
        loadHourlyDistribution();
        loadPlayerPlaytimes();
        loadPlayerLevels();
    }

    private void saveData() {
        saveOnlinePlayerHistory();
        saveHourlyDistribution();
        savePlayerPlaytimes();
        savePlayerLevels();
    }

    private void loadOnlinePlayerHistory() {
        Path historyFile = dataFolder.resolve("online_history.csv");

        if (Files.exists(historyFile)) {
            try {
                List<String> lines = Files.readAllLines(historyFile);
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        try {
                            long timestamp = Long.parseLong(parts[0]);
                            int count = Integer.parseInt(parts[1]);
                            onlinePlayerHistory.put(timestamp, count);
                        } catch (NumberFormatException e) {
                            logger.warning("Invalid data format in online history: " + line);
                        }
                    }
                }
                logger.info("Loaded " + onlinePlayerHistory.size() + " online history records");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to load online player history", e);
            }
        }
    }

    private void saveOnlinePlayerHistory() {
        Path historyFile = dataFolder.resolve("online_history.csv");

        try {
            long cutoffTime = System.currentTimeMillis() - (config.getDataRetentionDays() * 24 * 60 * 60 * 1000L);
            List<String> lines = onlinePlayerHistory.entrySet().stream()
                    .filter(entry -> entry.getKey() >= cutoffTime)
                    .map(entry -> entry.getKey() + "," + entry.getValue())
                    .collect(Collectors.toList());

            Files.write(historyFile, lines);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save online player history", e);
        }
    }

    private void loadHourlyDistribution() {
        Path distributionFile = dataFolder.resolve("hourly_distribution.csv");

        if (Files.exists(distributionFile)) {
            try {
                List<String> lines = Files.readAllLines(distributionFile);
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        String hour = parts[0];
                        String date = parts[1];
                        try {
                            int count = Integer.parseInt(parts[2]);
                            hourlyDistribution.computeIfAbsent(hour, k -> new HashMap<>()).put(date, count);
                        } catch (NumberFormatException e) {
                            logger.warning("Invalid data format in hourly distribution: " + line);
                        }
                    }
                }
                logger.info("Loaded hourly distribution data");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to load hourly distribution", e);
            }
        }
    }

    private void saveHourlyDistribution() {
        Path distributionFile = dataFolder.resolve("hourly_distribution.csv");

        try {
            List<String> lines = new ArrayList<>();
            for (Map.Entry<String, Map<String, Integer>> hourEntry : hourlyDistribution.entrySet()) {
                String hour = hourEntry.getKey();
                for (Map.Entry<String, Integer> dateEntry : hourEntry.getValue().entrySet()) {
                    String date = dateEntry.getKey();
                    int count = dateEntry.getValue();
                    lines.add(hour + "," + date + "," + count);
                }
            }
            Files.write(distributionFile, lines);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save hourly distribution", e);
        }
    }

    private void loadPlayerPlaytimes() {
        Path playtimesFile = dataFolder.resolve("player_playtimes.csv");

        if (Files.exists(playtimesFile)) {
            try {
                List<String> lines = Files.readAllLines(playtimesFile);
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        try {
                            UUID uuid = UUID.fromString(parts[0]);
                            long playtime = Long.parseLong(parts[1]);
                            playerPlaytimes.put(uuid, playtime);
                        } catch (IllegalArgumentException e) {
                            logger.warning("Invalid data format in player playtimes: " + line);
                        }
                    }
                }
                logger.info("Loaded " + playerPlaytimes.size() + " player playtime records");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to load player playtimes", e);
            }
        }
    }

    private void savePlayerPlaytimes() {
        Path playtimesFile = dataFolder.resolve("player_playtimes.csv");

        try {
            List<String> lines = playerPlaytimes.entrySet().stream()
                    .map(entry -> entry.getKey() + "," + entry.getValue())
                    .collect(Collectors.toList());

            Files.write(playtimesFile, lines);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save player playtimes", e);
        }
    }

    private void loadPlayerLevels() {
        Path levelsFile = dataFolder.resolve("player_levels.csv");

        if (Files.exists(levelsFile)) {
            try {
                List<String> lines = Files.readAllLines(levelsFile);
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        try {
                            UUID uuid = UUID.fromString(parts[0]);
                            int level = Integer.parseInt(parts[1]);
                            playerLevels.put(uuid, level);
                        } catch (IllegalArgumentException e) {
                            logger.warning("Invalid data format in player levels: " + line);
                        }
                    }
                }
                logger.info("Loaded " + playerLevels.size() + " player level records");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to load player levels", e);
            }
        }
    }

    private void savePlayerLevels() {
        Path levelsFile = dataFolder.resolve("player_levels.csv");

        try {
            List<String> lines = playerLevels.entrySet().stream()
                    .map(entry -> entry.getKey() + "," + entry.getValue())
                    .collect(Collectors.toList());

            Files.write(levelsFile, lines);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save player levels", e);
        }
    }

    public Map<String, Object> getStatsSummary() {
        Map<String, Object> summary = new HashMap<>();

        int totalPlayers = Bukkit.getOfflinePlayers().length;
        summary.put("totalPlayers", totalPlayers);

        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        summary.put("onlinePlayers", onlinePlayers);

        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        int newPlayers = countNewPlayers(oneDayAgo);
        summary.put("newPlayers", newPlayers);

        long totalPlaytime = calculateTotalPlaytime();
        long avgPlaytime = totalPlayers > 0 ? totalPlaytime / totalPlayers : 0;
        summary.put("avgPlaytime", avgPlaytime);

        if (config.isEnableTpsTracking()) {
            summary.put("tps", tpsTracker.getTPSString());
        }

        return summary;
    }

    public List<OnlinePlayerData> getOnlinePlayersData(int days) {
        List<OnlinePlayerData> data = new ArrayList<>();
        long cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L);

        for (Map.Entry<Long, Integer> entry : onlinePlayerHistory.entrySet()) {
            long timestamp = entry.getKey();
            int playerCount = entry.getValue();

            if (timestamp >= cutoffTime) {
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(timestamp), serverTimeZone);

                OnlinePlayerData point = OnlinePlayerData.builder()
                        .timestamp(timestamp)
                        .online(playerCount)
                        .hour(dateTime.getHour())
                        .day(dateTime.getDayOfWeek().toString())
                        .build();

                data.add(point);
            }
        }

        data.sort(Comparator.comparing(OnlinePlayerData::getTimestamp));
        return data;
    }

    public Map<String, Integer> getCurrentHourlyDistribution() {
        Map<String, Integer> result = new TreeMap<>();

        for (int i = 0; i < 24; i++) {
            LocalDateTime time = LocalDateTime.now().withHour(i).withMinute(0);
            String hourStr = time.format(timeFormatter);
            result.put(hourStr, 0);
        }

        String today = LocalDateTime.now().format(dateFormatter);

        for (Map.Entry<String, Map<String, Integer>> entry : hourlyDistribution.entrySet()) {
            String hour = entry.getKey();
            Map<String, Integer> dateCounts = entry.getValue();

            if (dateCounts.containsKey(today)) {
                result.put(hour, dateCounts.get(today));
            }
        }

        LocalDateTime now = LocalDateTime.now();
        String currentHour = now.format(timeFormatter);
        result.put(currentHour, Bukkit.getOnlinePlayers().size());

        return result;
    }

    public List<TopPlayerData> getTopPlayers(int limit) {
        if (limit > config.getMaxTopPlayersLimit()) {
            limit = config.getMaxTopPlayersLimit();
        }

        List<TopPlayerData> result = new ArrayList<>();
        Map<UUID, Long> combinedPlaytimes = new HashMap<>(playerPlaytimes);

        long currentTime = System.currentTimeMillis();
        for (Map.Entry<UUID, Long> entry : playerSessions.entrySet()) {
            UUID uuid = entry.getKey();
            long sessionStart = entry.getValue();
            long sessionDuration = currentTime - sessionStart;

            combinedPlaytimes.put(uuid, combinedPlaytimes.getOrDefault(uuid, 0L) + sessionDuration);
        }

        List<Map.Entry<UUID, Long>> sortedPlayers = new ArrayList<>(combinedPlaytimes.entrySet());
        sortedPlayers.sort(Map.Entry.<UUID, Long>comparingByValue().reversed());

        int count = 0;
        for (Map.Entry<UUID, Long> entry : sortedPlayers) {
            if (count >= limit) break;

            UUID uuid = entry.getKey();
            long playtime = entry.getValue();

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String name = offlinePlayer.getName();

            if (name == null) continue;

            TopPlayerData.TopPlayerDataBuilder builder = TopPlayerData.builder()
                    .name(name)
                    .uuid(uuid.toString())
                    .playtime(playtime)
                    .online(offlinePlayer.isOnline());

            if (offlinePlayer.isOnline()) {
                Player player = offlinePlayer.getPlayer();
                if (player != null) {
                    builder.level(player.getLevel())
                           .health(player.getHealth());
                }
            }

            result.add(builder.build());
            count++;
        }

        return result;
    }

    private int countNewPlayers(long since) {
        int count = 0;
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (player.getFirstPlayed() >= since) {
                count++;
            }
        }
        return count;
    }

    public Long getPlayerPlaytime(String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            return null;
        }
        
        UUID uuid = offlinePlayer.getUniqueId();
        long storedPlaytime = playerPlaytimes.getOrDefault(uuid, 0L);
        
        if (playerSessions.containsKey(uuid)) {
            long sessionStart = playerSessions.get(uuid);
            long sessionDuration = System.currentTimeMillis() - sessionStart;
            return storedPlaytime + sessionDuration;
        }
        
        return storedPlaytime;
    }
    
    public Integer getPlayerLevel(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline()) {
            return player.getLevel();
        }
        
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer.hasPlayedBefore()) {
            UUID uuid = offlinePlayer.getUniqueId();
            return playerLevels.get(uuid);
        }
        
        return null;
    }

    private long calculateTotalPlaytime() {
        Map<UUID, Long> combinedPlaytimes = new HashMap<>(playerPlaytimes);

        long currentTime = System.currentTimeMillis();
        for (Map.Entry<UUID, Long> entry : playerSessions.entrySet()) {
            UUID uuid = entry.getKey();
            long sessionStart = entry.getValue();
            long sessionDuration = currentTime - sessionStart;

            combinedPlaytimes.put(uuid, combinedPlaytimes.getOrDefault(uuid, 0L) + sessionDuration);
        }

        return combinedPlaytimes.values().stream().mapToLong(Long::longValue).sum();
    }
}