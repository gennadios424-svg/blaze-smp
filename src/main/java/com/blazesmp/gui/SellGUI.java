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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class SellGUI implements Listener {
    private static final String TITLE = "§2Blaze SMP §8| §aSell Items";
    private static final int SELL_SLOTS = 45;
    private final JavaPlugin plugin;
    private final WorthService worth;
    private final MoneyService money;
    private final Set<Player> processing = new HashSet<>();

    public SellGUI(JavaPlugin plugin, WorthService worth, MoneyService money) {
        this.plugin = plugin; this.worth = worth; this.money = money;
    }

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
            }
            return;
        }

        if (raw >= SELL_SLOTS && raw < top.getSize()) {
            event.setCancelled(true);
            if (raw == 49) processSell(player, top);
            else if (raw == 48) clearSellSlots(player, top);
            return;
        }

        if (raw < 0) return;
        if (event.isShiftClick() && raw >= top.getSize()) {
            event.setCancelled(true);
            ItemStack source = event.getCurrentItem();
            if (source != null && !source.getType().isAir()) addToSell(top, source.clone());
            event.setCurrentItem(null);
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
        if (!(event.getInventory().getHolder() instanceof SellHolder)) return;
        returnItems(player, event.getInventory());
    }

    @EventHandler public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof SellHolder) {
            returnItems(event.getPlayer(), event.getPlayer().getOpenInventory().getTopInventory());
        }
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
        try {
            long total = 0L;
            for (int i = 0; i < SELL_SLOTS; i++) {
                ItemStack item = inv.getItem(i);
                if (item == null || item.getType().isAir()) continue;
                Long price = worth.getSellPrice(item.getType());
                if (price == null) {
                    player.sendMessage("§cCannot sell " + ItemUtil.pretty(item.getType()) + ". It has no configured sell price.");
                    return;
                }
                long value = worth.calculate(item.getType(), item.getAmount());
                if (value == Long.MAX_VALUE || Long.MAX_VALUE - total < value) {
                    player.sendMessage("§cTransaction is too large.");
                    return;
                }
                total += value;
            }
            if (total <= 0) {
                player.sendMessage("§cPut items in the sell slots first.");
                return;
            }
            // Server-side second calculation is deliberately performed immediately before mutation.
            long verifiedTotal = 0L;
            for (int i = 0; i < SELL_SLOTS; i++) {
                ItemStack item = inv.getItem(i);
                if (item == null || item.getType().isAir()) continue;
                long value = worth.calculate(item.getType(), item.getAmount());
                if (value == Long.MAX_VALUE || Long.MAX_VALUE - verifiedTotal < value) {
                    player.sendMessage("§cTransaction is too large."); return;
                }
                verifiedTotal += value;
            }
            if (verifiedTotal != total) return;
            for (int i = 0; i < SELL_SLOTS; i++) inv.setItem(i, null);
            try {
                money.deposit(player, verifiedTotal);
            } catch (RuntimeException ex) {
                // Restore everything if the money transaction cannot complete.
                player.sendMessage("§cSale failed safely; your items were restored.");
                return;
            }
            player.sendMessage("§aSold your items for §f" + MoneyService.format(verifiedTotal) + "§a.");
            player.closeInventory();
        } finally {
            processing.remove(player);
        }
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
        for (ItemStack left : player.getInventory().addItem(item).values()) player.getWorld().dropItemNaturally(player.getLocation(), left);
    }

    private void addToSell(Inventory inv, ItemStack incoming) {
        if (incoming == null || incoming.getType().isAir()) return;
        int remaining = incoming.getAmount();
        int max = incoming.getMaxStackSize();
        for (int i = 0; i < SELL_SLOTS && remaining > 0; i++) {
            ItemStack existing = inv.getItem(i);
            if (existing != null && existing.isSimilar(incoming) && existing.getAmount() < max) {
                int move = Math.min(remaining, max - existing.getAmount());
                existing.setAmount(existing.getAmount() + move); remaining -= move;
            }
        }
        for (int i = 0; i < SELL_SLOTS && remaining > 0; i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType().isAir()) {
                int move = Math.min(remaining, max);
                ItemStack part = incoming.clone(); part.setAmount(move); inv.setItem(i, part); remaining -= move;
            }
        }
        // Caller must never lose an item that did not fit.
        if (remaining > 0) {
            ItemStack leftover = incoming.clone(); leftover.setAmount(remaining);
            // Put overflow back into the player's inventory on the next tick.
            Player holder = null;
            for (Player p : Bukkit.getOnlinePlayers()) if (p.getOpenInventory().getTopInventory() == inv) { holder = p; break; }
            if (holder != null) giveOrDrop(holder, leftover);
        }
    }

    private void swapWithHotbar(Player player, Inventory inv, int slot, int hotbar) {
        if (hotbar < 0 || hotbar > 8) return;
        ItemStack hot = player.getInventory().getItem(hotbar);
        ItemStack top = inv.getItem(slot);
        if (hot == null || hot.getType().isAir()) return;
        inv.setItem(slot, hot);
        player.getInventory().setItem(hotbar, top);
    }

    private void updateButtons(Inventory inv) {
        long total = 0L;
        boolean invalid = false;
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
        inv.setItem(45, ItemUtil.button(Material.PAPER, "§aCurrent Total", java.util.List.of("§f" + MoneyService.format(total), invalid ? "§cContains an unsupported item" : "§7Server will recalculate on sale")));
        inv.setItem(48, ItemUtil.button(Material.BARRIER, "§cClear", java.util.List.of("§7Return all sell-slot items")));
        inv.setItem(49, ItemUtil.button(Material.EMERALD, "§aSELL", java.util.List.of("§7Sell total: §f" + MoneyService.format(total), "§7Click to complete transaction")));
        inv.setItem(50, ItemUtil.button(Material.BOOK, "§eHow it works", java.util.List.of("§7Put items in the top slots.", "§7Shift-click from your inventory works.", "§7Closing returns unsold items.")));
    }

    private static final class SellHolder implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }
}
