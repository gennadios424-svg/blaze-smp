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
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!service.isShardGainer(tool)) return;

        int progress = service.increment(tool);
        int required = service.getRequiredBlocks();
        if (progress < required) {
            player.sendActionBar("§bShardgainer: §f" + progress + "§7/§f" + required);
            return;
        }

        if (!service.rollReward()) {
            service.reset(tool);
            player.sendMessage("§7Shardgainer: §f" + required + " blocks processed§7, but no shard reward this cycle.");
            return;
        }

        int amount = service.rewardAmount();
        service.giveShards(player, tool, amount);
        player.sendMessage("§b§lShardgainer §7» §aYou received §f" + amount + " Emerald Shard" + (amount == 1 ? "" : "s") + "§a!");
    }
}
