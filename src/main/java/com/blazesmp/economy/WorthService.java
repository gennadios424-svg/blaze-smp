package com.blazesmp.economy;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class WorthService {
    private final JavaPlugin plugin;
    private final Map<Material, Long> sellPrices = new EnumMap<>(Material.class);

    private static final Set<Material> EXCLUDED = Set.of(
            Material.AIR, Material.CAVE_AIR, Material.VOID_AIR,
            Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK,
            Material.COMMAND_BLOCK_MINECART, Material.JIGSAW, Material.STRUCTURE_BLOCK,
            Material.STRUCTURE_VOID, Material.BARRIER, Material.LIGHT,
            Material.DEBUG_STICK, Material.KNOWLEDGE_BOOK, Material.SPAWNER,
            Material.TEST_INSTANCE_BLOCK
    );

    public WorthService(JavaPlugin plugin) { this.plugin = plugin; }

    public void reload() {
        sellPrices.clear();
        for (Material material : Material.values()) {
            if (isEconomyMaterial(material)) sellPrices.put(material, defaultPrice(material));
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
        plugin.getLogger().info("Loaded " + sellPrices.size() + " legitimate Java item worth entries.");
    }

    private boolean isEconomyMaterial(Material material) {
        return material != null && !EXCLUDED.contains(material) && material.isItem();
    }

    /*
     * Full-item coverage is generated from Paper's Material registry rather than a
     * tiny hand-maintained list. Explicit config anchors override these defaults.
     */
    private long defaultPrice(Material material) {
        String n = material.name();

        // Economy anchors / very rare progression items.
        if (n.equals("BEACON")) return 400_000L;
        if (n.equals("DRAGON_EGG")) return 2_000_000L;
        if (n.equals("NETHERITE_BLOCK")) return 250_000L;
        if (n.equals("ANCIENT_DEBRIS")) return 15_000L;
        if (n.equals("NETHERITE_SCRAP")) return 12_000L;
        if (n.equals("NETHERITE_INGOT")) return 50_000L;
        if (n.equals("NETHER_STAR")) return 25_000L;
        if (n.equals("ELYTRA")) return 15_000L;
        if (n.equals("SHULKER_BOX")) return 200L;
        if (n.equals("TOTEM_OF_UNDYING")) return 5_000L;
        if (n.equals("END_CRYSTAL")) return 3_500L;
        if (n.equals("WITHER_SKELETON_SKULL")) return 8_000L;
        if (n.equals("ENCHANTED_GOLDEN_APPLE")) return 7_500L;
        if (n.equals("GOLDEN_APPLE")) return 1_500L;

        // Existing economy anchors.
        if (n.equals("KELP")) return 200L;
        if (n.equals("SEA_PICKLE")) return 150L;
        if (n.equals("PINK_PETALS")) return 125L;
        if (n.equals("DIAMOND_HOE")) return 250L;
        if (n.equals("CHEST")) return 25L;
        if (n.equals("TRAPPED_CHEST")) return 75L;
        if (n.equals("CHEST_MINECART")) return 250L;
        if (n.endsWith("_CHEST_BOAT")) return 50L;
        if (n.equals("NETHERITE_BLOCK")) return 250_000L;

        // Rare Nether / End progression.
        if (n.contains("CRYING_OBSIDIAN")) return 250L;
        if (n.equals("OBSIDIAN")) return 175L;
        if (n.contains("NETHER_QUARTZ")) return 80L;
        if (n.equals("QUARTZ")) return 80L;
        if (n.contains("GLOWSTONE")) return 100L;
        if (n.contains("BLAZE")) return 400L;
        if (n.contains("GHAST_TEAR")) return 1_500L;
        if (n.contains("MAGMA_CREAM")) return 350L;
        if (n.contains("NETHER_WART")) return 175L;
        if (n.contains("END_STONE")) return 120L;
        if (n.contains("PURPUR")) return 160L;
        if (n.contains("CHORUS")) return 140L;
        if (n.contains("SHULKER")) return 600L;
        if (n.contains("ENDER_PEARL")) return 300L;
        if (n.equals("END_PORTAL_FRAME")) return 0L;

        // Ores and refined resources.
        if (n.equals("DIAMOND_BLOCK")) return 4_500L;
        if (n.equals("EMERALD_BLOCK")) return 3_000L;
        if (n.equals("GOLD_BLOCK")) return 1_200L;
        if (n.equals("IRON_BLOCK")) return 600L;
        if (n.equals("COPPER_BLOCK")) return 250L;
        if (n.equals("LAPIS_BLOCK")) return 500L;
        if (n.equals("REDSTONE_BLOCK")) return 500L;
        if (n.equals("COAL_BLOCK")) return 350L;
        if (n.equals("RAW_IRON_BLOCK")) return 500L;
        if (n.equals("RAW_GOLD_BLOCK")) return 900L;
        if (n.equals("RAW_COPPER_BLOCK")) return 200L;
        if (n.equals("DIAMOND")) return 500L;
        if (n.equals("EMERALD")) return 350L;
        if (n.equals("GOLD_INGOT")) return 150L;
        if (n.equals("IRON_INGOT")) return 75L;
        if (n.equals("COPPER_INGOT")) return 40L;
        if (n.equals("LAPIS_LAZULI")) return 60L;
        if (n.equals("REDSTONE")) return 25L;
        if (n.equals("COAL")) return 35L;
        if (n.equals("RAW_IRON")) return 65L;
        if (n.equals("RAW_GOLD")) return 125L;
        if (n.equals("RAW_COPPER")) return 35L;

        if (n.endsWith("_ORE")) return n.startsWith("DIAMOND") ? 600L
                : n.startsWith("EMERALD") ? 450L
                : n.startsWith("GOLD") ? 180L
                : n.startsWith("IRON") ? 90L
                : n.startsWith("COPPER") ? 50L
                : n.startsWith("LAPIS") ? 75L
                : n.startsWith("REDSTONE") ? 35L : 45L;

        // Mob / utility drops.
        if (n.contains("SPAWN_EGG")) return 250L;
        if (n.equals("SLIME_BALL")) return 150L;
        if (n.equals("PRISMARINE_SHARD")) return 90L;
        if (n.equals("PRISMARINE_CRYSTALS")) return 120L;
        if (n.equals("NAUTILUS_SHELL")) return 1_000L;
        if (n.equals("HEART_OF_THE_SEA")) return 5_000L;
        if (n.equals("DRAGON_BREATH")) return 750L;
        if (n.equals("EXPERIENCE_BOTTLE")) return 500L;

        // Crafted gear/items: valuable, but below major progression artifacts.
        if (n.contains("NETHERITE")) return 1_000L;
        if (n.contains("DIAMOND")) return 500L;
        if (n.contains("EMERALD")) return 350L;
        if (n.contains("GOLDEN_")) return 175L;
        if (n.contains("IRON_")) return 75L;

        // Common renewable materials should remain low enough to avoid economy abuse.
        if (n.equals("DIRT") || n.equals("COBBLESTONE") || n.equals("STONE") || n.equals("GRAVEL")
                || n.equals("SAND") || n.equals("RED_SAND") || n.equals("NETHERRACK")
                || n.equals("DEEPSLATE")) return 5L;
        if (n.contains("LOG") || n.contains("WOOD") || n.contains("PLANKS")) return 12L;
        if (n.contains("WOOL") || n.contains("CARPET")) return 15L;
        if (n.contains("GLASS")) return 12L;
        if (n.contains("LEATHER")) return 35L;
        if (n.contains("PAPER")) return 10L;
        if (n.contains("BOOK")) return 30L;
        if (n.contains("BREAD") || n.contains("POTATO") || n.contains("CARROT") || n.contains("BEETROOT")) return 15L;
        if (n.contains("FISH") || n.contains("COD") || n.contains("SALMON")) return 25L;

        if (material.isBlock()) return 10L;
        return 20L;
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
