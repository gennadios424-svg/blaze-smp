package com.blazesmp.command;

import com.blazesmp.economy.MoneyService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class EcoCommand implements CommandExecutor {
    private final MoneyService money;
    public EcoCommand(MoneyService money) { this.money = money; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("blazesmp.eco")) { sender.sendMessage("§cNo permission."); return true; }
        if (args.length != 3) { sender.sendMessage("§cUsage: /eco <give|take|set> <player> <amount>"); return true; }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        long amount;
        try { amount = Long.parseLong(args[2]); } catch (NumberFormatException e) { sender.sendMessage("§cInvalid amount."); return true; }
        if (amount < 0) { sender.sendMessage("§cAmount cannot be negative."); return true; }
        long current = money.getBalance(target);
        try {
            switch (args[0].toLowerCase()) {
                case "give" -> money.deposit(target, amount);
                case "take" -> money.setBalance(target, Math.max(0L, current - amount));
                case "set" -> money.setBalance(target, amount);
                default -> { sender.sendMessage("§cUsage: /eco <give|take|set> <player> <amount>"); return true; }
            }
        } catch (RuntimeException e) { sender.sendMessage("§cTransaction failed: " + e.getMessage()); return true; }
        sender.sendMessage("§aUpdated " + target.getName() + "'s balance to §f" + MoneyService.format(money.getBalance(target)));
        return true;
    }
}
