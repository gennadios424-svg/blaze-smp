package com.blazesmp.economy;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class WorthService {
    private final JavaPlugin plugin;
    private final Map<Material, Long> sellPrices = new EnumMap<>(Material.class);

    // These are Bukkit/Paper materials that are not player-legitimate economy items.
    // Everything else that is a real item is automatically included, so new Java items
    // added by Paper do not require a tiny hand-maintained worth list.
    private static final Set<Material> EXCLUDED = Set.of(
            Material.AIR, Material.CAVE_AIR, Material.VOID_AIR,
            Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK,
            Material.COMMAND_BLOCK_MINECART, Material.JIGSAW, Material.STRUCTURE_BLOCK,
            Material.STRUCTURE_VOID, Material.BARRIER, Material.LIGHT,
            Material.DEBUG_STICK, Material.KNOWLEDGE_BOOK, Material.SPAWNER,
            Material.TEST_INSTANCE_BLOCK, Material.TEST_INSTANCE_BLOCK
    );

    public WorthService(JavaPlugin plugin) { this.plugin = plugin; }

    public void reload() {
        sellPrices.clear();

        // Populate every legitimate current Paper Material that represents a usable item.
        // Explicit config values always win, preserving the server's economy anchors.
        for (Material material : Material.values()) {
            if (!isEconomyMaterial(material)) continue;
            sellPrices.put(material, defaultPrice(material));
        }

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("worth");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase(Locale.ROOT));
                    if (!isEconomyMaterial(material)) continue;
                    long price = section.getLong(key + ".sell", -1L);
                    if (price >= 0) sellPrices.put(material, price);
                } catch (IllegalArgumentException ignored) {
                    plugin.getLogger().warning("Unknown worth material: " + key);
                }
            }
        }

        plugin.getLogger().info("Loaded " + sellPrices.size() + " obtainable Java item worth entries.");
    }

    private boolean isEconomyMaterial(Material material) {
        return material != null && !EXCLUDED.contains(material) && material.isItem();
    }

    private long defaultPrice(Material material) {
        String n = material.name();

        // Preserve sensible economy tiers while guaranteeing a valid numeric price
        // for every automatically discovered player item.
        if (n.equals("CHEST")) return 25L;
        if (n.equals("TRAPPED_CHEST")) return 75L;
        if (n.equals("CHEST_MINECART")) return 250L;
        if (n.endsWith("_CHEST_BOAT")) return 50L;

        if (n.equals("NETHERITE_BLOCK")) return 250_000L;
        if (n.equals("DRAGON_EGG")) return 2_000_000L;
        if (n.equals("SHULKER_BOX")) return 200L;
        if (n.contains("NETHERITE")) return 1_000L;
        if (n.contains("DIAMOND")) return 500L;
        if (n.contains("EMERALD")) return 350L;
        if (n.contains("GOLD")) return 150L;
        if (n.contains("IRON")) return 75L;
        if (n.contains("COPPER")) return 40L;
        if (n.contains("REDSTONE")) return 25L;
        if (n.contains("LAPIS")) return 25L;
        if (n.contains("QUARTZ")) return 25L;
        if (n.contains("AMETHYST")) return 20L;

        if (n.contains("ORE") || n.contains("RAW_")) return 20L;
        if (n.contains("SPAWN_EGG")) return 100L;
        if (n.contains("TOTEM")) return 5_000L;
        if (n.contains("ELYTRA")) return 10_000L;
        if (n.contains("NETHER_STAR")) return 25_000L;
        if (n.contains("ENCHANTED_BOOK")) return 500L;

        if (material.isBlock()) return 10L;
        return 5L;
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
            if (q.isEmpty() || material.name().toLowerCase(Locale.ROOT).contains(q)
                    || pretty(material).toLowerCase(Locale.ROOT).contains(q)) result.add(material);
        }
        result.sort(Comparator.comparing(Enum::name));
        return result;
    }

    private String pretty(Material material) {
        String[] parts = material.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    public List<Material> all() { return search(""); }
}
