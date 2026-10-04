package com.blazesmp.command;

import com.blazesmp.rank.Rank;
import com.blazesmp.rank.RankService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class RankCommand implements CommandExecutor {
    private final RankService ranks;

    public RankCommand(RankService ranks) {
        this.ranks = ranks;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            sender.sendMessage("§6" + target.getName() + "§7's rank: §f" + ranks.getRank(target).displayName());
            return true;
        }

        if (args.length != 3 || !sender.hasPermission("blazesmp.rank.manage")) {
            sender.sendMessage("§cUsage: /rank <player> OR /rank set <player> <OWNER|DEV|MOD|MEDIA|MEMBER>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Rank newRank = Rank.parse(args[2]);
        if (newRank == null) {
            sender.sendMessage("§cInvalid rank.");
            return true;
        }

        Rank actor = sender instanceof org.bukkit.entity.Player player
                ? ranks.getRank(player)
                : Rank.OWNER;

        if (sender instanceof org.bukkit.entity.Player && !actor.canManage(newRank)) {
            sender.sendMessage("§cYou cannot assign a rank equal to or higher than your own.");
            return true;
        }

        ranks.setRank(target, newRank);
        sender.sendMessage("§aSet §f" + target.getName() + "§a to §f" + newRank.displayName() + "§a.");
        return true;
    }
}
