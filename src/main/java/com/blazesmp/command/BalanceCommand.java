package com.blazesmp.command;

import com.blazesmp.economy.MoneyService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BalanceCommand implements CommandExecutor {
    private final MoneyService money;
    public BalanceCommand(MoneyService money) { this.money = money; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        player.sendMessage("§aYour balance: §f" + MoneyService.format(money.getBalance(player)));
        return true;
    }
}
