package com.example.scoreboard.managers;

import com.example.scoreboard.ScoreboardPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoneyManager {

    private final ScoreboardPlugin plugin;

    // Vault economy hook — null if Vault is not installed
    private Economy vaultEconomy = null;

    // Fallback storage used only when Vault is not present
    private final Map<UUID, Double> balances = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public MoneyManager(ScoreboardPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "money.yml");

        // Try to hook into Vault
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp =
                    Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                vaultEconomy = rsp.getProvider();
                plugin.getLogger().info("[Money] Hooked into Vault economy: " + vaultEconomy.getName());
            } else {
                plugin.getLogger().warning("[Money] Vault found but no economy plugin registered! Falling back to built-in money.");
                loadFallback();
            }
        } else {
            plugin.getLogger().info("[Money] Vault not found — using built-in money system.");
            loadFallback();
        }
    }

    /** Returns true if we are using Vault. */
    public boolean isVaultEnabled() {
        return vaultEconomy != null;
    }

    /** Get a player's balance — from Vault if available, otherwise fallback. */
    public double getBalance(UUID uuid) {
        if (vaultEconomy != null) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            return vaultEconomy.getBalance(op);
        }

        // Player already loaded in memory — return directly
        if (balances.containsKey(uuid)) {
            return balances.get(uuid);
        }

        // Check if player exists in the saved file (returning player after restart)
        if (dataConfig != null && dataConfig.contains("balances." + uuid)) {
            double saved = dataConfig.getDouble("balances." + uuid);
            balances.put(uuid, saved);
            return saved;
        }

        // Truly new player — give starting money and save immediately
        double starting = plugin.getConfig().getDouble("starting-money", 100.0);
        balances.put(uuid, starting);
        saveFallback();
        plugin.getLogger().info("[Money] New player " + uuid + " given " + format(starting));
        return starting;
    }

    /** Format a balance with the configured symbol. */
    public String format(double amount) {
        if (vaultEconomy != null) {
            return vaultEconomy.format(amount);
        }
        String symbol = plugin.getConfig().getString("money-symbol", "$");
        return symbol + String.format("%.2f", amount);
    }

    // ── Fallback save/load (only used when Vault is absent) ───────────────
    private void loadFallback() {
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

        // Load all saved balances into memory
        if (dataConfig.isConfigurationSection("balances")) {
            for (String key : dataConfig.getConfigurationSection("balances").getKeys(false)) {
                try {
                    balances.put(UUID.fromString(key), dataConfig.getDouble("balances." + key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        plugin.getLogger().info("[Money] Loaded " + balances.size() + " balance(s) from money.yml");
    }

    private void saveFallback() {
        dataConfig = new YamlConfiguration();
        balances.forEach((uuid, amount) -> dataConfig.set("balances." + uuid, amount));
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[Money] Could not save money.yml: " + e.getMessage());
        }
    }

    /** Called by ScoreboardPlugin.saveAll() — only does anything for the fallback system. */
    public void saveData() {
        if (vaultEconomy == null) saveFallback();
        // Vault manages its own persistence — nothing to do here
    }
}
