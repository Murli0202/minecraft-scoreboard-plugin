package com.example.scoreboard.listeners;

import com.example.scoreboard.ScoreboardPlugin;
import com.example.scoreboard.managers.StatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StatsListener implements Listener {

    private final ScoreboardPlugin plugin;
    private final StatManager statManager;

    public StatsListener(ScoreboardPlugin plugin, StatManager statManager) {
        this.plugin = plugin;
        this.statManager = statManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player killed = event.getEntity();
        statManager.addDeath(killed.getUniqueId());

        Player killer = killed.getKiller();
        if (killer != null) {
            statManager.addKill(killer.getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Update scoreboard
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                plugin.getScoreboardManager().update(player), 5L);

        // Notify OPs about available updates (2 second delay so it appears after join messages)
        if (player.isOp() && plugin.isUpdateAvailable()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("§6§l[ScoreboardPlugin] §eNew version available: §av" + plugin.getNewestVersion());
                player.sendMessage("§7Current: §cv" + plugin.getPluginVersion() + " §7→ New: §av" + plugin.getNewestVersion());
                player.sendMessage("§7Download: §bhttps://github.com/Murli0202/scoreboard-plugin/releases/latest");
            }, 40L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getScoreboardManager().remove(event.getPlayer().getUniqueId());
        plugin.saveAll();
    }
}
