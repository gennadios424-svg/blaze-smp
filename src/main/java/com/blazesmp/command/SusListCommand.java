package com.blazesmp.command;

import com.blazesmp.anticheat.AntiCheatService;
import com.blazesmp.anticheat.Suspect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class SusListCommand implements CommandExecutor {
    private final AntiCheatService antiCheat;

    public SusListCommand(AntiCheatService antiCheat) {
        this.antiCheat = antiCheat;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("blazesmp.suslist")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        Map<?, Suspect> suspects = antiCheat.getSuspects();
        if (suspects.isEmpty()) {
            sender.sendMessage("§a§lSUSLIST §8» §7No players are currently flagged.");
            return true;
        }

        sender.sendMessage("§c§lSUSLIST §8» §7Flagged players: §f" + suspects.size());
        for (Suspect suspect : suspects.values()) {
            sender.sendMessage("§c• §f" + suspect.getPlayerName() + " §8| §cScore: §f" + suspect.getScore());
            for (Map.Entry<String, Integer> reason : suspect.getReasons().entrySet()) {
                sender.sendMessage("  §8- §7" + reason.getKey() + " §8(x" + reason.getValue() + ")");
            }
        }
        return true;
    }
}
