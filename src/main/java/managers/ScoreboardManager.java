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
        // BUG FIX 1: Bukkit.getOnlinePlayers() returns a live collection —
        // copy it first to avoid ConcurrentModificationException if a player
        // joins/leaves mid-iteration on the main thread.
        for (Player player : List.copyOf(Bukkit.getOnlinePlayers())) {
            update(player);
        }
    }

    public void update(Player player) {
        // BUG FIX 2: player.isOnline() guard — the scheduled update (5L delay
        // on join) could fire after the player already disconnected, causing
        // a NullPointerException when calling player.setScoreboard().
        if (!player.isOnline()) return;

        Scoreboard board = boards.computeIfAbsent(player.getUniqueId(),
                k -> Bukkit.getScoreboardManager().getNewScoreboard());

        String rawTitle = plugin.getConfig().getString("title", "&6&lMurli Network");
        // BUG FIX 3: color() BEFORE truncating — truncating the raw string
        // could cut mid color-code and produce garbled output. Translate first,
        // then truncate the visible result.
        String title = color(rawTitle);
        if (title.length() > 32) title = title.substring(0, 32);

        // BUG FIX 4: unregister the old objective BEFORE registering the new one.
        // Registering with the same name while the old one still exists throws
        // IllegalArgumentException on some Paper builds.
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

        // BUG FIX 5: empty lines list means config is missing or broken —
        // return a safe fallback instead of showing a blank/broken scoreboard.
        if (template.isEmpty()) {
            return List.of(color("&cNo lines configured in config.yml"));
        }

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

        // BUG FIX 6: a rank entry in config.yml with a missing "display" key
        // would cause a NullPointerException inside color(). Guard both fields.
        for (Map<?, ?> rankMap : ranks) {
            String permission = (String) rankMap.get("permission");
            String display    = (String) rankMap.get("display");
            if (permission == null || display == null) continue;
            if (player.hasPermission(permission)) {
                return color(display);
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
