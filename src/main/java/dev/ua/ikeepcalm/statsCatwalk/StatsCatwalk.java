package dev.ua.ikeepcalm.statsCatwalk;

import dev.ua.ikeepcalm.statsCatwalk.api.StatsEndpoint;
import dev.ua.ikeepcalm.statsCatwalk.config.StatsConfig;
import dev.ua.ikeepcalm.statsCatwalk.listener.StatsListener;
import dev.ua.ikeepcalm.statsCatwalk.manager.StatsManager;
import dev.ua.uaproject.catwalk.hub.webserver.services.CatWalkWebserverService;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class StatsCatwalk extends JavaPlugin {

    @Getter
    @Setter
    private static StatsCatwalk instance;

    private StatsConfig statsConfig;
    private StatsManager statsManager;
    private StatsListener statsListener;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        statsConfig = new StatsConfig(this);

        statsManager = new StatsManager(this, statsConfig);
        statsListener = new StatsListener(statsManager);

        getServer().getPluginManager().registerEvents(statsListener, this);

        CatWalkWebserverService webserverService = Bukkit.getServicesManager().load(CatWalkWebserverService.class);

        if (webserverService == null) {
            getLogger().severe("Failed to load CatWalkWebserverService from Bukkit ServicesManager.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        webserverService.registerHandlers(new StatsEndpoint(statsManager));

        log("StatsCatwalk has been enabled!");
    }

    @Override
    public void onDisable() {
        if (statsManager != null) {
            statsManager.stop();
        }
        log("StatsCatwalk has been disabled!");
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[StatsCatwalk] " + ChatColor.WHITE + message);
    }

    public static void error(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[StatsCatwalk] " + ChatColor.WHITE + message);
    }
}