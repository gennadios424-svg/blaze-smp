package com.blazesmp.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class ItemUtil {
    private ItemUtil() {}

    public static ItemStack button(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore == null ? new ArrayList<>() : lore);
        item.setItemMeta(meta);
        return item;
    }

    public static String pretty(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }
}
