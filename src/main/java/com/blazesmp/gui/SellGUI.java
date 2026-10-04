package com.blazesmp.gui;

import com.blazesmp.economy.MoneyService;
import com.blazesmp.economy.WorthService;
import com.blazesmp.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SellGUI implements Listener {
    private static final String TITLE = "§6§l🔥 BLAZE SMP §8| §e§lSELL ITEMS";
    private static final int SELL_SLOTS = 45;
    private final JavaPlugin plugin;
    private final WorthService worth;
    private final MoneyService money;
    private final Set<Player> processing = new HashSet<>();

    public SellGUI(JavaPlugin plugin, WorthService worth, MoneyService money) { this.plugin = plugin; this.worth = worth; this.money = money; }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(new SellHolder(), 54, TITLE);
        updateButtons(inv);
        player.openInventory(inv);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof SellHolder)) return;
        int raw = event.getRawSlot();
        if (raw >= 0 && raw < SELL_SLOTS) {
            if (event.getClick() == ClickType.NUMBER_KEY) {
                event.setCancelled(true);
                swapWithHotbar(player, top, raw, event.getHotbarButton());
                updateButtons(top);
            } else if (event.getClick() == ClickType.DOUBLE_CLICK) {
                event.setCancelled(true);
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> updateButtons(top));
            }
            return;
        }
        if (raw >= SELL_SLOTS && raw < top.getSize()) {
            event.setCancelled(true);
            if (raw == 49) processSell(player, top);
            else if (raw == 48) clearSellSlots(player, top);
            return;
        }
        if (raw >= top.getSize() && event.isShiftClick()) {
            event.setCancelled(true);
            ItemStack source = event.getCurrentItem();
            if (source != null && !source.getType().isAir()) {
                int original = source.getAmount();
                int moved = addToSell(top, source.clone());
                if (moved >= original) event.setCurrentItem(null);
                else if (moved > 0) source.setAmount(original - moved);
            }
            updateButtons(top);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof SellHolder)) return;
        for (int slot : event.getRawSlots()) {
            if (slot >= event.getView().getTopInventory().getSize() || slot >= SELL_SLOTS) {
                event.setCancelled(true);
                return;
            }
        }
        Bukkit.getScheduler().runTask(plugin, () -> updateButtons(event.getView().getTopInventory()));
    }

    @EventHandler public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (event.getInventory().getHolder() instanceof SellHolder) returnItems(player, event.getInventory());
    }

    @EventHandler public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof SellHolder)
            returnItems(event.getPlayer(), event.getPlayer().getOpenInventory().getTopInventory());
    }

    public void closeAllAndReturnItems() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof SellHolder) {
                returnItems(player, player.getOpenInventory().getTopInventory());
                player.closeInventory();
            }
        }
    }

    private void processSell(Player player, Inventory inv) {
        if (!processing.add(player)) return;
        List<ItemStack> snapshot = new ArrayList<>();
        try {
            long total = calculateTotal(inv);
            if (total < 0) {
                player.sendMessage("§cThis sell contains an unsupported item or an invalidly large transaction. Nothing was removed.");
                return;
            }
            if (total == 0) { player.sendMessage("§cPut items in the sell slots first."); return; }
            for (int i = 0; i < SELL_SLOTS; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && !item.getType().isAir()) snapshot.add(item.clone());
            }
            long verifiedTotal = calculateTotal(inv);
            if (verifiedTotal != total) { player.sendMessage("§cTransaction changed during validation. Nothing was sold."); return; }
            for (int i = 0; i < SELL_SLOTS; i++) inv.setItem(i, null);
            try { money.deposit(player, verifiedTotal); }
            catch (RuntimeException ex) {
                for (ItemStack item : snapshot) giveOrDrop(player, item);
                player.sendMessage("§cSale failed safely; your items were restored.");
                return;
            }
            player.sendMessage("§aSold your items for §f" + MoneyService.format(verifiedTotal) + "§a.");
            player.closeInventory();
        } finally { processing.remove(player); }
    }

    private long calculateTotal(Inventory inv) {
        long total = 0L;
        for (int i = 0; i < SELL_SLOTS; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir()) continue;
            Long price = worth.getSellPrice(item.getType());
            if (price == null) return -1L;
            long value = worth.calculate(item.getType(), item.getAmount());
            if (value == Long.MAX_VALUE || Long.MAX_VALUE - total < value) return -1L;
            total += value;
        }
        return total;
    }

    private void clearSellSlots(Player player, Inventory inv) {
        for (int i = 0; i < SELL_SLOTS; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && !item.getType().isAir()) giveOrDrop(player, item);
            inv.setItem(i, null);
        }
        updateButtons(inv);
    }

    private void returnItems(Player player, Inventory inv) {
        for (int i = 0; i < SELL_SLOTS; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && !item.getType().isAir()) giveOrDrop(player, item);
            inv.setItem(i, null);
        }
    }

    private void giveOrDrop(Player player, ItemStack item) {
        for (ItemStack left : player.getInventory().addItem(item).values())
            player.getWorld().dropItemNaturally(player.getLocation(), left);
    }

    private int addToSell(Inventory inv, ItemStack incoming) {
        if (incoming == null || incoming.getType().isAir()) return 0;
        int remaining = incoming.getAmount(), max = incoming.getMaxStackSize();
        for (int i = 0; i < SELL_SLOTS && remaining > 0; i++) {
            ItemStack existing = inv.getItem(i);
            if (existing != null && existing.isSimilar(incoming) && existing.getAmount() < max) {
                int move = Math.min(remaining, max - existing.getAmount());
                existing.setAmount(existing.getAmount() + move);
                remaining -= move;
            }
        }
        for (int i = 0; i < SELL_SLOTS && remaining > 0; i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType().isAir()) {
                int move = Math.min(remaining, max);
                ItemStack part = incoming.clone();
                part.setAmount(move);
                inv.setItem(i, part);
                remaining -= move;
            }
        }
        return incoming.getAmount() - remaining;
    }

    private void swapWithHotbar(Player player, Inventory inv, int slot, int hotbar) {
        if (hotbar < 0 || hotbar > 8) return;
        ItemStack hot = player.getInventory().getItem(hotbar), top = inv.getItem(slot);
        if (hot == null || hot.getType().isAir()) return;
        inv.setItem(slot, hot.clone());
        player.getInventory().setItem(hotbar, top == null ? null : top.clone());
    }

    private void updateButtons(Inventory inv) {
        long total = 0L; boolean invalid = false;
        for (int i = 0; i < SELL_SLOTS; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir()) continue;
            Long price = worth.getSellPrice(item.getType());
            if (price == null) invalid = true;
            else {
                long value = worth.calculate(item.getType(), item.getAmount());
                if (value == Long.MAX_VALUE || Long.MAX_VALUE - total < value) invalid = true;
                else total += value;
            }
        }
        inv.setItem(45, ItemUtil.button(Material.GOLD_INGOT, "§e§lTOTAL", List.of(
                "§7You will receive: §e§l" + MoneyService.format(total),
                invalid ? "§cContains an unsupported item" : "§7All prices use WorthService"
        )));
        inv.setItem(48, ItemUtil.button(Material.BARRIER, "§6§lCLEAR", List.of("§7Return all sell-slot items")));
        inv.setItem(49, ItemUtil.button(Material.ORANGE_DYE, "§6§l🔥 SELL ITEMS", List.of(
                "§7Sell total: §e§l" + MoneyService.format(total),
                "§eClick to complete the sale"
        )));
        inv.setItem(50, ItemUtil.button(Material.YELLOW_STAINED_GLASS_PANE, "§eHow it works",
                List.of("§7Put items in the top slots.", "§7Shift-click from inventory works.", "§7Closing returns unsold items.")));
        inv.setItem(46, ItemUtil.button(Material.ORANGE_STAINED_GLASS_PANE, "§6", List.of()));
        inv.setItem(47, ItemUtil.button(Material.ORANGE_STAINED_GLASS_PANE, "§6", List.of()));
        inv.setItem(51, ItemUtil.button(Material.ORANGE_STAINED_GLASS_PANE, "§6", List.of()));
        inv.setItem(52, ItemUtil.button(Material.ORANGE_STAINED_GLASS_PANE, "§6", List.of()));
    }

    private static final class SellHolder implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }
}
