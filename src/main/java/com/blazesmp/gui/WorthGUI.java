package com.blazesmp.gui;

import com.blazesmp.economy.MoneyService;
import com.blazesmp.economy.WorthService;
import com.blazesmp.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class WorthGUI implements Listener {
    private static final String TITLE = "§2Blaze SMP §8| §aItem Worth";
    private final JavaPlugin plugin;
    private final WorthService worth;
    private final Map<UUID, String> awaitingSearch = new HashMap<>();

    public WorthGUI(JavaPlugin plugin, WorthService worth) { this.plugin = plugin; this.worth = worth; }
    public void open(Player player, String query) { openPage(player, query == null ? "" : query, 0); }

    private void openPage(Player player, String query, int page) {
        List<Material> materials = worth.search(query);
        int pages = Math.max(1, (materials.size() + 44) / 45);
        page = Math.max(0, Math.min(page, pages - 1));
        Inventory inv = Bukkit.createInventory(new WorthHolder(query, page), 54, TITLE + " §7(" + (page + 1) + "/" + pages + ")");
        int start = page * 45;
        for (int i = 0; i < 45 && start + i < materials.size(); i++) {
            Material mat = materials.get(start + i);
            long price = worth.getSellPrice(mat);
            inv.setItem(i, ItemUtil.button(mat, "§a" + ItemUtil.pretty(mat), List.of("§7Sell value: §f" + MoneyService.format(price) + " §7each", "§7Stack: §f" + MoneyService.format(worth.calculate(mat, mat.getMaxStackSize())))));
        }
        inv.setItem(45, ItemUtil.button(Material.ARROW, "§ePrevious Page", List.of("§7Page " + Math.max(1, page))));
        inv.setItem(49, ItemUtil.button(Material.COMPASS, "§bSearch", List.of("§7Click and type an item name in chat")));
        inv.setItem(53, ItemUtil.button(Material.ARROW, "§eNext Page", List.of("§7Page " + Math.min(pages, page + 2))));
        player.openInventory(inv);
    }

    public void showItem(Player player, String raw) {
        String q = raw.toLowerCase(Locale.ROOT).replace(' ', '_');
        Material exact = null;
        try { exact = Material.valueOf(q.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ignored) { }
        if (exact != null && worth.getSellPrice(exact) != null) {
            long price = worth.getSellPrice(exact);
            player.sendMessage("§a§l" + ItemUtil.pretty(exact));
            player.sendMessage("§7Sell Value: §f" + MoneyService.format(price) + " §7each");
            player.sendMessage("§7Full stack: §f" + MoneyService.format(worth.calculate(exact, exact.getMaxStackSize())));
            return;
        }
        List<Material> matches = worth.search(raw);
        if (matches.size() == 1) { showItem(player, matches.get(0).name()); return; }
        if (matches.isEmpty()) player.sendMessage("§cNo priced item found for §f" + raw + "§c.");
        else open(player, raw);
    }

    public void openSearchPrompt(Player player) {
        awaitingSearch.put(player.getUniqueId(), "");
        player.closeInventory();
        player.sendMessage("§bType an item name to search the worth list. Type §fcancel §bto stop.");
    }

    @EventHandler public void onChat(AsyncPlayerChatEvent event) {
        if (!awaitingSearch.containsKey(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
        String query = event.getMessage().trim();
        awaitingSearch.remove(event.getPlayer().getUniqueId());
        if (query.equalsIgnoreCase("cancel")) return;
        Bukkit.getScheduler().runTask(plugin, () -> open(event.getPlayer(), query));
    }

    @EventHandler public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof WorthHolder holder)) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot == 49) openSearchPrompt(player);
        else if (slot == 45) openPage(player, holder.query, holder.page - 1);
        else if (slot == 53) openPage(player, holder.query, holder.page + 1);
    }

    @EventHandler public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof WorthHolder) event.setCancelled(true);
    }

    private static final class WorthHolder implements org.bukkit.inventory.InventoryHolder {
        private final String query;
        private final int page;
        private WorthHolder(String query, int page) { this.query = query; this.page = page; }
        @Override public Inventory getInventory() { return null; }
    }
}
