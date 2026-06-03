package com.example.scoreboard.managers;

import com.example.scoreboard.ScoreboardPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatManager {

    private final ScoreboardPlugin plugin;
    private final Map<UUID, Integer> kills  = new HashMap<>();
    private final Map<UUID, Integer> deaths = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public StatManager(ScoreboardPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "stats.yml");
        loadData();
    }

    public int getKills(UUID uuid)  { return kills.getOrDefault(uuid, 0); }
    public int getDeaths(UUID uuid) { return deaths.getOrDefault(uuid, 0); }

    public void addKill(UUID uuid) {
        kills.put(uuid, getKills(uuid) + 1);
        saveData(); // save immediately after every kill/death
    }

    public void addDeath(UUID uuid) {
        deaths.put(uuid, getDeaths(uuid) + 1);
        saveData();
    }

    public void loadData() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
                plugin.getLogger().info("[Stats] stats.yml created.");
            } catch (IOException e) {
                plugin.getLogger().severe("[Stats] Could not create stats.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        kills.clear();
        deaths.clear();

        if (dataConfig.contains("kills")) {
            for (String key : dataConfig.getConfigurationSection("kills").getKeys(false)) {
                try { kills.put(UUID.fromString(key), dataConfig.getInt("kills." + key)); }
                catch (IllegalArgumentException ignored) {}
            }
        }
        if (dataConfig.contains("deaths")) {
            for (String key : dataConfig.getConfigurationSection("deaths").getKeys(false)) {
                try { deaths.put(UUID.fromString(key), dataConfig.getInt("deaths." + key)); }
                catch (IllegalArgumentException ignored) {}
            }
        }
        plugin.getLogger().info("[Stats] Loaded " + kills.size() + " kill record(s).");
    }

    public void saveData() {
        // Rebuild config from scratch to avoid stale entries
        dataConfig = new YamlConfiguration();
        kills.forEach((uuid, count)  -> dataConfig.set("kills."  + uuid, count));
        deaths.forEach((uuid, count) -> dataConfig.set("deaths." + uuid, count));
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[Stats] Could not save stats.yml: " + e.getMessage());
        }
    }
}
