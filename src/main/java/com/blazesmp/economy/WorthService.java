package com.blazesmp.economy;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class WorthService {
    private final JavaPlugin plugin;
    private final Map<Material, Long> sellPrices = new EnumMap<>(Material.class);

    public WorthService(JavaPlugin plugin) { this.plugin = plugin; }

    public void reload() {
        sellPrices.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("worth");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            try {
                Material material = Material.valueOf(key.toUpperCase(Locale.ROOT));
                long price = section.getLong(key + ".sell", -1L);
                if (price >= 0) sellPrices.put(material, price);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Unknown worth material: " + key);
            }
        }
    }

    public Long getSellPrice(Material material) { return sellPrices.get(material); }

    public long calculate(Material material, int amount) {
        Long price = getSellPrice(material);
        if (price == null || amount <= 0) return 0L;
        if (price != 0 && amount > Long.MAX_VALUE / price) return Long.MAX_VALUE;
        return price * amount;
    }

    public List<Material> search(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Material> result = new ArrayList<>();
        for (Material material : sellPrices.keySet()) {
            if (q.isEmpty() || material.name().toLowerCase(Locale.ROOT).contains(q)) result.add(material);
        }
        result.sort(Comparator.comparing(Enum::name));
        return result;
    }

    public List<Material> all() { return search(""); }
}
