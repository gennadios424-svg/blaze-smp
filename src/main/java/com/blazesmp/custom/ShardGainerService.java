package com.blazesmp.custom;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public final class ShardGainerService {
    private final JavaPlugin plugin;
    private final NamespacedKey idKey, counterKey;
    private final Random random = new Random();

    public ShardGainerService(JavaPlugin plugin) {
        this.plugin = plugin;
        idKey = new NamespacedKey(plugin, "custom_item_id");
        counterKey = new NamespacedKey(plugin, "shardgainer_blocks");
    }

    public ItemStack createShardGainer() {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lShardgainer");
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(idKey, PersistentDataType.STRING, "BLAZE_SHARDGAINER");
        pdc.set(counterKey, PersistentDataType.INTEGER, 0);
        meta.setLore(java.util.List.of("§7Every 64 blocks: chance for 1–5 Emerald Shards", "§8Progress is stored on this item"));
        item.setItemMeta(meta);
        return item;
    }

    public boolean isShardGainer(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        return "BLAZE_SHARDGAINER".equals(item.getItemMeta().getPersistentDataContainer()
                .get(idKey, PersistentDataType.STRING));
    }

    public int increment(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int count = pdc.getOrDefault(counterKey, PersistentDataType.INTEGER, 0) + 1;
        pdc.set(counterKey, PersistentDataType.INTEGER, count);
        item.setItemMeta(meta);
        return count;
    }

    public void reset(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(counterKey, PersistentDataType.INTEGER, 0);
        item.setItemMeta(meta);
    }

    public int progress(ItemStack item) {
        if (!isShardGainer(item)) return 0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(counterKey, PersistentDataType.INTEGER, 0);
    }

    public boolean rollReward() {
        double chance = plugin.getConfig().getDouble("shardgainer.reward-chance", 25.0);
        return random.nextDouble() < Math.max(0.0, Math.min(100.0, chance)) / 100.0;
    }

    public int rewardAmount() {
        Map<Integer,Integer> weights = new LinkedHashMap<>();
        weights.put(1, plugin.getConfig().getInt("shardgainer.rewards.1", 60));
        weights.put(2, plugin.getConfig().getInt("shardgainer.rewards.2", 20));
        weights.put(3, plugin.getConfig().getInt("shardgainer.rewards.3", 10));
        weights.put(4, plugin.getConfig().getInt("shardgainer.rewards.4", 7));
        weights.put(5, plugin.getConfig().getInt("shardgainer.rewards.5", 3));
        int total = weights.values().stream().mapToInt(Integer::intValue).sum();
        int roll = random.nextInt(Math.max(1,total));
        int running = 0;
        for (Map.Entry<Integer,Integer> e : weights.entrySet()) {
            running += Math.max(0,e.getValue());
            if (roll < running) return e.getKey();
        }
        return 1;
    }

    public void giveShards(Player player, int amount) {
        // Compatibility layer for the server's Emerald Shard item/currency:
        // emeralds are represented as the existing emerald resource rather than money.
        ItemStack shards = new ItemStack(Material.EMERALD, amount);
        Map<Integer, ItemStack> left = player.getInventory().addItem(shards);
        for (ItemStack stack : left.values())
            player.getWorld().dropItemNaturally(player.getLocation(), stack);
    }
}
