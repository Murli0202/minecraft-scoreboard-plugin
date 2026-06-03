package com.example.scoreboard.managers;

import com.example.scoreboard.ScoreboardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class ScoreboardManager {

    private final ScoreboardPlugin plugin;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardManager(ScoreboardPlugin plugin) {
        this.plugin = plugin;
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            update(player);
        }
    }

    public void update(Player player) {
        Scoreboard board = boards.computeIfAbsent(player.getUniqueId(),
                k -> Bukkit.getScoreboardManager().getNewScoreboard());

        String rawTitle = plugin.getConfig().getString("title", "&6&lMurli Network");
        String title = color(rawTitle);
        if (title.length() > 32) title = title.substring(0, 32);

        Objective obj = board.getObjective("sidebar");
        if (obj != null) obj.unregister();
        obj = board.registerNewObjective("sidebar", Criteria.DUMMY, title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = buildLines(player);

        // Assign scores in reverse so first line = top
        int score = lines.size();
        Set<String> usedEntries = new HashSet<>();
        for (String line : lines) {
            // Ensure unique entries by padding with invisible color codes
            String entry = line;
            while (usedEntries.contains(entry)) entry += ChatColor.RESET;
            usedEntries.add(entry);
            obj.getScore(entry).setScore(score--);
        }

        player.setScoreboard(board);
    }

    private List<String> buildLines(Player player) {
        String money = plugin.getMoneyManager().format(
                plugin.getMoneyManager().getBalance(player.getUniqueId()));
        String tokens = String.valueOf(
                plugin.getTokenManager().getTokens(player.getUniqueId()));
        String kills = String.valueOf(
                plugin.getStatManager().getKills(player.getUniqueId()));
        String deaths = String.valueOf(
                plugin.getStatManager().getDeaths(player.getUniqueId()));
        String ping = player.getPing() + "ms";
        String playerCount = Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers();
        String rank = getRank(player);

        List<String> template = plugin.getConfig().getStringList("lines");
        List<String> result = new ArrayList<>();
        for (String line : template) {
            line = line
                    .replace("{money}", money)
                    .replace("{tokens}", tokens)
                    .replace("{kills}", kills)
                    .replace("{deaths}", deaths)
                    .replace("{ping}", ping)
                    .replace("{players}", playerCount)
                    .replace("{rank}", rank)
                    .replace("{player}", player.getName());
            result.add(color(line));
        }
        return result;
    }

    public String getRank(Player player) {
        List<Map<?, ?>> ranks = plugin.getConfig().getMapList("ranks");
        String defaultRank = color(plugin.getConfig().getString("default-rank", "&7Member"));
        for (Map<?, ?> rankMap : ranks) {
            String permission = (String) rankMap.get("permission");
            String display = color((String) rankMap.get("display"));
            if (permission != null && player.hasPermission(permission)) {
                return display;
            }
        }
        return defaultRank;
    }

    public void remove(UUID uuid) {
        Scoreboard board = boards.remove(uuid);
        if (board != null) {
            Objective obj = board.getObjective("sidebar");
            if (obj != null) obj.unregister();
        }
    }

    public void removeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        boards.clear();
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
