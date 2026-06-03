package com.example.scoreboard.commands;

import com.example.scoreboard.ScoreboardPlugin;
import com.example.scoreboard.managers.TokenManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MurliTokenCommand implements CommandExecutor {

    private final ScoreboardPlugin plugin;
    private final TokenManager tokenManager;

    public MurliTokenCommand(ScoreboardPlugin plugin, TokenManager tokenManager) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp() && !sender.hasPermission("scoreboard.token.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        // /murlitoken give <player> <amount>
        // /murlitoken take <player> <amount>
        // /murlitoken set <player> <amount>
        // /murlitoken check <player>

        if (args.length < 2) {
            sender.sendMessage("§6§lMurliToken Commands:");
            sender.sendMessage("§e/murlitoken give <player> <amount>");
            sender.sendMessage("§e/murlitoken take <player> <amount>");
            sender.sendMessage("§e/murlitoken set <player> <amount>");
            sender.sendMessage("§e/murlitoken check <player>");
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage("§cPlayer §f" + args[1] + " §cis not online.");
            return true;
        }

        if (action.equals("check")) {
            sender.sendMessage("§f" + target.getName() + " §ehas §6" +
                    tokenManager.getTokens(target.getUniqueId()) + " §eMurliTokens.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /murlitoken " + action + " <player> <amount>");
            return true;
        }

        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount: §f" + args[2]);
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage("§cAmount must be greater than 0.");
            return true;
        }

        switch (action) {
            case "give" -> {
                tokenManager.addTokens(target.getUniqueId(), amount);
                sender.sendMessage("§aGave §6" + amount + " §aMurliTokens to §f" + target.getName() + "§a.");
                target.sendMessage("§aYou received §6" + amount + " §aMurliTokens!");
            }
            case "take" -> {
                boolean success = tokenManager.removeTokens(target.getUniqueId(), amount);
                if (!success) {
                    sender.sendMessage("§f" + target.getName() + " §cdoesn't have enough MurliTokens.");
                } else {
                    sender.sendMessage("§aTook §6" + amount + " §aMurliTokens from §f" + target.getName() + "§a.");
                    target.sendMessage("§c" + amount + " §cMurliTokens were taken from you.");
                }
            }
            case "set" -> {
                tokenManager.setTokens(target.getUniqueId(), amount);
                sender.sendMessage("§aSet §f" + target.getName() + "§a's MurliTokens to §6" + amount + "§a.");
                target.sendMessage("§aYour MurliTokens were set to §6" + amount + "§a.");
            }
            default -> sender.sendMessage("§cUnknown action. Use: give, take, set, check.");
        }

        // Refresh scoreboard
        plugin.getScoreboardManager().update(target);
        return true;
    }
}
