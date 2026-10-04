package com.blazesmp.custom;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class DrillListener implements Listener {
    private final DrillService drill;
    private final Set<UUID> processing = new HashSet<>();

    public DrillListener(DrillService drill) { this.drill = drill; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp() || !drill.isDrill(player.getInventory().getItemInMainHand())) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (drill.isExpired(tool)) {
            player.sendMessage("§cThe 3×3 Drill has expired.");
            return;
        }
        if (!processing.add(player.getUniqueId())) return;

        try {
            event.setCancelled(true);
            Block center = event.getBlock();
            BlockFace face = player.getFacing();
            int[][] offsets;
            if (face == BlockFace.UP || face == BlockFace.DOWN)
                offsets = new int[][]{{-1,0},{0,0},{1,0}};
            else
                offsets = new int[][]{{-1,-1},{-1,0},{-1,1},{0,-1},{0,0},{0,1},{1,-1},{1,0},{1,1}};

            for (int[] o : offsetsFor(face, offsets)) {
                Block target = center.getRelative(o[0], o[1], o[2]);
                if (!drill.canBreak(target)) continue;
                org.bukkit.event.block.BlockBreakEvent child = new org.bukkit.event.block.BlockBreakEvent(target, player);
                Bukkit.getPluginManager().callEvent(child);
                if (child.isCancelled()) continue;
                target.breakNaturally(tool);
            }
        } finally {
            processing.remove(player.getUniqueId());
        }
    }

    private int[][] offsetsFor(BlockFace face, int[][] unused) {
        int[][] result = new int[9][3];
        int k = 0;
        for (int a=-1;a<=1;a++) for (int b=-1;b<=1;b++) {
            if (face == BlockFace.UP || face == BlockFace.DOWN) result[k++] = new int[]{a,0,b};
            else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) result[k++] = new int[]{a,b,0};
            else result[k++] = new int[]{0,b,a};
        }
        return result;
    }
}
