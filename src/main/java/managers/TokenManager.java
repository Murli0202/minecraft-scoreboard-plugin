package com.example.scoreboard.managers;

import com.example.scoreboard.ScoreboardPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TokenManager {

    private final ScoreboardPlugin plugin;
    private final Map<UUID, Long> tokens = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public TokenManager(ScoreboardPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "tokens.yml");
        loadData();
    }

    public long getTokens(UUID uuid) {
        return tokens.getOrDefault(uuid, 0L);
    }

    public void addTokens(UUID uuid, long amount) {
        tokens.put(uuid, getTokens(uuid) + amount);
        saveData(); // save immediately after every change
    }

    public void setTokens(UUID uuid, long amount) {
        tokens.put(uuid, Math.max(0, amount));
        saveData();
    }

    public boolean removeTokens(UUID uuid, long amount) {
        if (getTokens(uuid) < amount) return false;
        tokens.put(uuid, getTokens(uuid) - amount);
        saveData();
        return true;
    }

    public void loadData() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
                plugin.getLogger().info("[Tokens] tokens.yml created.");
            } catch (IOException e) {
                plugin.getLogger().severe("[Tokens] Could not create tokens.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        tokens.clear();
        if (dataConfig.contains("tokens")) {
            for (String key : dataConfig.getConfigurationSection("tokens").getKeys(false)) {
                try {
                    tokens.put(UUID.fromString(key), dataConfig.getLong("tokens." + key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        plugin.getLogger().info("[Tokens] Loaded " + tokens.size() + " player(s).");
    }

    public void saveData() {
        // Clear old data first so deleted entries don't persist
        dataConfig = new YamlConfiguration();
        tokens.forEach((uuid, amount) -> dataConfig.set("tokens." + uuid, amount));
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[Tokens] Could not save tokens.yml: " + e.getMessage());
        }
    }
}
