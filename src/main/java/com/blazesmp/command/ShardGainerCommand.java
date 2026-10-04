package com.blazesmp.command;

import com.blazesmp.custom.ShardGainerService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ShardGainerCommand implements CommandExecutor {
    private final ShardGainerService service;
    public ShardGainerCommand(ShardGainerService service) { this.service = service; }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Only players can receive a Shardgainer."); return true; }
        if (!player.isOp()) { player.sendMessage("§cOnly OPs can access the Shardgainer."); return true; }
        player.getInventory().addItem(service.createShardGainer());
        player.sendMessage("§aYou received a §fShardgainer§a.");
        return true;
    }
}
