package com.blazesmp.command;

import com.blazesmp.economy.ShardService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class ShardCommand implements CommandExecutor {
    private final ShardService shards;

    public ShardCommand(ShardService shards) {
        this.shards = shards;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof org.bukkit.entity.Player player)) {
                sender.sendMessage("§cUsage: /shards <player> or /shards give|take|set <player> <amount>");
                return true;
            }
            sender.sendMessage("§b§l🔥 Blaze Shards");
            sender.sendMessage("§7Balance: §f" + ShardService.format(shards.getBalance(player)) + " Blaze Shards");
            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("blazesmp.shards.others")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            sender.sendMessage("§b" + target.getName() + "§7 has §f" + ShardService.format(shards.getBalance(target)) + " Blaze Shards.");
            return true;
        }

        if (args.length != 3 || !sender.hasPermission("blazesmp.shards.admin")) {
            sender.sendMessage("§cUsage: /shards give|take|set <player> <amount>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount.");
            return true;
        }
        if (amount < 0) {
            sender.sendMessage("§cAmount cannot be negative.");
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "give" -> shards.deposit(target, amount);
                case "take" -> shards.setBalance(target, Math.max(0L, shards.getBalance(target) - amount));
                case "set" -> shards.setBalance(target, amount);
                default -> {
                    sender.sendMessage("§cUsage: /shards give|take|set <player> <amount>");
                    return true;
                }
            }
        } catch (RuntimeException e) {
            sender.sendMessage("§cTransaction failed: " + e.getMessage());
            return true;
        }

        sender.sendMessage("§aUpdated §f" + target.getName() + "§a's Blaze Shard balance to §f" +
                ShardService.format(shards.getBalance(target)) + "§a.");
        return true;
    }
}
