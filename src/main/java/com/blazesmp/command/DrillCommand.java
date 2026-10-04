package com.blazesmp.command;

import com.blazesmp.custom.DrillService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class DrillCommand implements CommandExecutor {
    private final DrillService drill;
    public DrillCommand(DrillService drill) { this.drill = drill; }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Only players can receive a Drill."); return true; }
        if (!player.isOp()) { player.sendMessage("§cOnly OPs can access the 3×3 Drill."); return true; }
        player.getInventory().addItem(drill.createDrill());
        player.sendMessage("§aYou received a §f3×3 Drill§a. It expires 7 days from now.");
        return true;
    }
}
