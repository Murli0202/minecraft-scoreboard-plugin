package com.example.scoreboard;

import com.example.scoreboard.commands.MurliTokenCommand;
import com.example.scoreboard.listeners.StatsListener;
import com.example.scoreboard.managers.ScoreboardManager;
import com.example.scoreboard.managers.StatManager;
import com.example.scoreboard.managers.TokenManager;
import com.example.scoreboard.managers.MoneyManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ScoreboardPlugin extends JavaPlugin {

    // Current plugin version — bump this with each release
    private static final String PLUGIN_VERSION = "1.0.0";

    // Cached update check result
    private String newestVersion = null;
    private boolean updateAvailable = false;

    private ScoreboardManager scoreboardManager;
    private TokenManager tokenManager;
    private StatManager statManager;
    private MoneyManager moneyManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Load all data from disk on startup
        moneyManager     = new MoneyManager(this);
        tokenManager     = new TokenManager(this);
        statManager      = new StatManager(this);
        scoreboardManager = new ScoreboardManager(this);

        getServer().getPluginManager().registerEvents(new StatsListener(this, statManager), this);
        getCommand("murlitoken").setExecutor(new MurliTokenCommand(this, tokenManager));

        // Update scoreboard on interval
        long interval = getConfig().getLong("update-interval-ticks", 20L);
        getServer().getScheduler().runTaskTimer(this, scoreboardManager::updateAll, interval, interval);

        // Auto-save all data every 5 minutes (6000 ticks)
        getServer().getScheduler().runTaskTimer(this, this::saveAll, 6000L, 6000L);

        // Check for updates async on startup
        getServer().getScheduler().runTaskAsynchronously(this, this::checkForUpdates);

        getLogger().info("ScoreboardPlugin v" + PLUGIN_VERSION + " enabled! Data loaded from disk.");
    }

    @Override
    public void onDisable() {
        saveAll();
        if (scoreboardManager != null) scoreboardManager.removeAll();
        getLogger().info("ScoreboardPlugin disabled. All data saved.");
    }

    // ── Update Checker ─────────────────────────────────────────────────────
    public void checkForUpdates() {
        try {
            String apiUrl = "https://api.github.com/repos/Murli0202/scoreboard-plugin/releases/latest";
            HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/vnd.github+json");
            con.setRequestProperty("User-Agent", "ScoreboardPlugin/" + PLUGIN_VERSION);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            if (con.getResponseCode() != 200) {
                getLogger().info("[Update] Could not reach GitHub (code: " + con.getResponseCode() + ")");
                return;
            }

            Scanner sc = new Scanner(con.getInputStream(), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            while (sc.hasNextLine()) sb.append(sc.nextLine());
            String json = sb.toString();

            int start = json.indexOf("\"tag_name\":\"") + 12;
            int end   = json.indexOf("\"", start);
            if (start < 12 || end < 0) return;

            newestVersion = json.substring(start, end).replace("v", "").trim();

            if (!newestVersion.equals(PLUGIN_VERSION)) {
                updateAvailable = true;
                getLogger().info("[Update] New version available: v" + newestVersion + " (current: v" + PLUGIN_VERSION + ")");
                getLogger().info("[Update] https://github.com/Murli0202/scoreboard-plugin/releases/latest");
            } else {
                getLogger().info("[Update] Plugin is up to date (v" + PLUGIN_VERSION + ")");
            }

        } catch (IOException e) {
            getLogger().warning("[Update] Update check failed: " + e.getMessage());
        }
    }

    public boolean isUpdateAvailable()  { return updateAvailable; }
    public String  getNewestVersion()   { return newestVersion; }
    public String  getPluginVersion()   { return PLUGIN_VERSION; }
    // ───────────────────────────────────────────────────────────────────────

    /** Save all persistent data to disk. */
    public void saveAll() {
        if (tokenManager  != null) tokenManager.saveData();
        if (statManager   != null) statManager.saveData();
        if (moneyManager  != null) moneyManager.saveData();
        getLogger().info("[ScoreboardPlugin] Data auto-saved.");
    }

    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public TokenManager      getTokenManager()      { return tokenManager; }
    public StatManager       getStatManager()       { return statManager; }
    public MoneyManager      getMoneyManager()      { return moneyManager; }
}
