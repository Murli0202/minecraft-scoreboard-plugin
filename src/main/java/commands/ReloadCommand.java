package com.example.scoreboard.commands;

import com.example.scoreboard.ScoreboardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {

    private final ScoreboardPlugin plugin;

    public ReloadCommand(ScoreboardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("scoreboard.reload")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        sender.sendMessage("§7Reloading ScoreboardPlugin...");

        // 1. Reload config.yml from disk
        plugin.reloadConfig();

        // 2. Re-check for updates with new config in place
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, plugin::checkForUpdates);

        // 3. Refresh scoreboards for all online players so new
        //    lines/title/ranks take effect immediately
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getScoreboardManager().update(player);
        }

        sender.sendMessage("§a§lScoreboardPlugin reloaded!");
        sender.sendMessage("§7Title, lines, ranks and intervals updated for all online players.");

        // Log to console as well (useful if reloaded from console)
        if (!(sender instanceof Player)) return true;
        plugin.getLogger().info("Config reloaded by " + sender.getName());

        return true;
    }
}
