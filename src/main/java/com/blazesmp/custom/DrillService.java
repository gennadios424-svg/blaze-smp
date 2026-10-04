package com.blazesmp.custom;

import com.blazesmp.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public final class DrillService {
    private final JavaPlugin plugin;
    private final NamespacedKey idKey, receivedKey, expiryKey;

    public DrillService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.idKey = new NamespacedKey(plugin, "custom_item_id");
        this.receivedKey = new NamespacedKey(plugin, "received_timestamp");
        this.expiryKey = new NamespacedKey(plugin, "expiry_timestamp");
    }

    public ItemStack createDrill() {
        ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§l3×3 Drill");
        long received = System.currentTimeMillis();
        long expiry = received + Duration.ofDays(7).toMillis();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(idKey, PersistentDataType.STRING, "BLAZE_DRILL");
        pdc.set(receivedKey, PersistentDataType.LONG, received);
        pdc.set(expiryKey, PersistentDataType.LONG, expiry);
        meta.setLore(java.util.List.of("§7Breaks a 3×3 area", "§7Expires in 7 days", "§8Custom Blaze SMP tool"));
        item.setItemMeta(meta);
        return item;
    }

    public boolean isDrill(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        String id = item.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        return "BLAZE_DRILL".equals(id);
    }

    public boolean isExpired(ItemStack item) {
        if (!isDrill(item)) return true;
        Long expiry = item.getItemMeta().getPersistentDataContainer().get(expiryKey, PersistentDataType.LONG);
        return expiry == null || System.currentTimeMillis() >= expiry;
    }

    public long expiry(ItemStack item) {
        if (!isDrill(item)) return 0L;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(expiryKey, PersistentDataType.LONG, 0L);
    }

    public boolean canBreak(Block block) {
        if (block == null || block.getType().isAir()) return false;
        Material type = block.getType();
        return type != Material.BEDROCK && type != Material.BARRIER && type != Material.END_PORTAL_FRAME
                && type != Material.COMMAND_BLOCK && type != Material.CHAIN_COMMAND_BLOCK
                && type != Material.REPEATING_COMMAND_BLOCK && type != Material.STRUCTURE_BLOCK
                && type != Material.JIGSAW && type != Material.SPAWNER;
    }

    public Player requireOp(Player player) { return player.isOp() ? player : null; }
}
