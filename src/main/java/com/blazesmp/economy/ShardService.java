package com.blazesmp.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ShardService {
    private final JavaPlugin plugin;
    private final File file;
    private final Map<UUID, Long> balances = new HashMap<>();

    public ShardService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "shards.yml");
        load();
    }

    public synchronized long getBalance(OfflinePlayer player) {
        return balances.getOrDefault(player.getUniqueId(), 0L);
    }

    public synchronized void setBalance(OfflinePlayer player, long amount) {
        balances.put(player.getUniqueId(), Math.max(0L, amount));
        save();
    }

    public synchronized void deposit(OfflinePlayer player, long amount) {
        if (amount <= 0) return;
        long current = getBalance(player);
        if (Long.MAX_VALUE - current < amount) throw new IllegalArgumentException("Shard balance overflow");
        balances.put(player.getUniqueId(), current + amount);
        save();
    }

    public synchronized boolean withdraw(OfflinePlayer player, long amount) {
        if (amount <= 0) return true;
        long current = getBalance(player);
        if (current < amount) return false;
        balances.put(player.getUniqueId(), current - amount);
        save();
        return true;
    }

    public synchronized void save() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) return;
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {
            yaml.set("balances." + entry.getKey(), Math.max(0L, entry.getValue()));
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save shard balances: " + e.getMessage());
        }
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (yaml.getConfigurationSection("balances") == null) return;
        for (String key : yaml.getConfigurationSection("balances").getKeys(false)) {
            try {
                balances.put(UUID.fromString(key), Math.max(0L, yaml.getLong("balances." + key)));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public static String format(long amount) {
        return String.format("%,d", Math.max(0L, amount));
    }
}
