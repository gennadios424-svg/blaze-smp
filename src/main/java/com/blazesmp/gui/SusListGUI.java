package com.blazesmp.gui;

import com.blazesmp.anticheat.AntiCheatService;
import com.blazesmp.anticheat.Suspect;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class SusListGUI implements Listener {
    private static final String TITLE = "§8§lBlaze SMP §8» §c§lSus List";
    private final JavaPlugin plugin;
    private final AntiCheatService antiCheat;

    public SusListGUI(JavaPlugin plugin, AntiCheatService antiCheat) {
        this.plugin = plugin;
        this.antiCheat = antiCheat;
    }

    public void open(Player player) {
        antiCheat.cleanupExpired();
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        List<Suspect> list = new ArrayList<>(antiCheat.getSuspects().values());
        list.sort(Comparator.comparingLong(Suspect::getLastFlag).reversed());

        for (int i = 0; i < Math.min(list.size(), 45); i++) {
            Suspect suspect = list.get(i);
            OfflinePlayer target = Bukkit.getOfflinePlayer(suspect.getPlayerName());
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.setDisplayName("§c§l" + suspect.getPlayerName());
                List<String> lore = new ArrayList<>();
                lore.add("§8Blaze SMP Anti-Cheat");
                lore.add("");
                lore.add("§7Suspicion score: §c" + suspect.getScore());
                lore.add("§7Latest flag: §f" + formatTime(suspect.getLastFlag()));
                lore.add("");
                lore.add("§cFlags:");
                for (Map.Entry<String, Integer> reason : suspect.getReasons().entrySet()) {
                    lore.add("§8• §7" + reason.getKey() + " §8(x" + reason.getValue() + ")");
                }
                lore.add("");
                lore.add("§8Flags expire 24h after the latest flag");
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            inv.setItem(i, head);
        }

        ItemStack info = new ItemStack(Material.EMERALD);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§a§lBlaze Anti-Cheat");
            infoMeta.setLore(List.of("§7Newest flags appear first", "§7No automatic bans", "§7Flags expire after 24 hours"));
            info.setItemMeta(infoMeta);
        }
        inv.setItem(49, info);
        player.openInventory(inv);
    }

    private String formatTime(long time) {
        return new SimpleDateFormat("dd/MM HH:mm:ss").format(new Date(time));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (TITLE.equals(event.getView().getTitle())) event.setCancelled(true);
    }
}
