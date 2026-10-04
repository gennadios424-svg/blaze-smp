package com.blazesmp.custom;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public final class ShardGainerListener implements Listener {
    private final ShardGainerService service;

    public ShardGainerListener(ShardGainerService service) { this.service = service; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) return;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!service.isShardGainer(tool)) return;

        int progress = service.increment(tool);
        if (progress < 64) {
            player.sendActionBar("§bShardgainer: §f" + progress + "§7/§f64");
            return;
        }

        service.reset(tool);
        if (!service.rollReward()) {
            player.sendMessage("§7Shardgainer: §f64 blocks processed§7, but no shard reward this cycle.");
            return;
        }

        int amount = service.rewardAmount();
        service.giveShards(player, amount);
        player.sendMessage("§b§lShardgainer §7» §aYou received §f" + amount + " Emerald Shard" + (amount == 1 ? "" : "s") + "§a!");
    }
}
