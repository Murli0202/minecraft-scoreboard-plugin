package com.example.scoreboard.managers;

import com.example.scoreboard.ScoreboardPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoneyManager {

    private final ScoreboardPlugin plugin;
    private final Map<UUID, Double> balances = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public MoneyManager(ScoreboardPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "money.yml");
        loadData();
    }

    public double getBalance(UUID uuid) {
        // If new player, give starting money and save it
        if (!balances.containsKey(uuid)) {
            double starting = plugin.getConfig().getDouble("starting-money", 100.0);
            balances.put(uuid, starting);
            saveData();
        }
        return balances.get(uuid);
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, Math.max(0, amount));
        saveData();
    }

    public void addBalance(UUID uuid, double amount) {
        balances.put(uuid, getBalance(uuid) + amount);
        saveData();
    }

    public boolean removeBalance(UUID uuid, double amount) {
        if (getBalance(uuid) < amount) return false;
        balances.put(uuid, getBalance(uuid) - amount);
        saveData();
        return true;
    }

    public String format(double amount) {
        String symbol = plugin.getConfig().getString("money-symbol", "$");
        return symbol + String.format("%.2f", amount);
    }

    public void loadData() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
                plugin.getLogger().info("[Money] money.yml created.");
            } catch (IOException e) {
                plugin.getLogger().severe("[Money] Could not create money.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        balances.clear();
        if (dataConfig.contains("balances")) {
            for (String key : dataConfig.getConfigurationSection("balances").getKeys(false)) {
                try { balances.put(UUID.fromString(key), dataConfig.getDouble("balances." + key)); }
                catch (IllegalArgumentException ignored) {}
            }
        }
        plugin.getLogger().info("[Money] Loaded " + balances.size() + " balance(s).");
    }

    public void saveData() {
        dataConfig = new YamlConfiguration();
        balances.forEach((uuid, amount) -> dataConfig.set("balances." + uuid, amount));
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[Money] Could not save money.yml: " + e.getMessage());
        }
    }
}
