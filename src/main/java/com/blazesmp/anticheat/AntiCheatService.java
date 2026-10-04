package com.blazesmp.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class AntiCheatService implements Listener {
    private static final long FIVE_HOURS = 5L * 60L * 60L * 1000L;
    private static final long CLICK_WINDOW = 1000L;
    private final JavaPlugin plugin;
    private final Map<UUID, Suspect> suspects = new LinkedHashMap<>();
    private final Map<UUID, Long> stationarySince = new HashMap<>();
    private final Map<UUID, Location> lastLocation = new HashMap<>();
    private final Map<UUID, Deque<Long>> clicks = new HashMap<>();
    private final Map<UUID, Deque<Long>> headMoves = new HashMap<>();
    private final Map<UUID, Deque<Material>> valuableBlocks = new HashMap<>();

    public AntiCheatService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        boolean positionMoved = from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
        boolean headMoved = from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch();
        if (positionMoved) {
            stationarySince.remove(p.getUniqueId());
        } else if (headMoved) {
            stationarySince.putIfAbsent(p.getUniqueId(), System.currentTimeMillis());
            addWindowEvent(headMoves, p.getUniqueId(), 1000L);
            checkMacro(p);
        } else {
            stationarySince.putIfAbsent(p.getUniqueId(), System.currentTimeMillis());
            checkMacro(p);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        long now = System.currentTimeMillis();
        Deque<Long> q = clicks.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<>());
        q.addLast(now);
        trim(q, CLICK_WINDOW);
        if (q.size() >= 40) {
            flag(p, "AutoClicker (40+ clicks/sec)", 5);
            q.clear();
        }
        checkMacro(p);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player p) {
            checkMacro(p);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Material type = event.getBlock().getType();
        if (isValuable(type)) {
            Deque<Material> q = valuableBlocks.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<>());
            q.addLast(type);
            while (q.size() > 20) q.removeFirst();
            checkMiningPattern(p, q);
        }
        checkMacro(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        stationarySince.remove(id);
        lastLocation.remove(id);
        clicks.remove(id);
        headMoves.remove(id);
        valuableBlocks.remove(id);
    }

    private void checkMacro(Player p) {
        Long since = stationarySince.get(p.getUniqueId());
        if (since == null || System.currentTimeMillis() - since < FIVE_HOURS) return;
        Deque<Long> clickQ = clicks.get(p.getUniqueId());
        Deque<Long> headQ = headMoves.get(p.getUniqueId());
        if ((clickQ != null && !clickQ.isEmpty()) || (headQ != null && !headQ.isEmpty())) {
            flag(p, "Stationary 5h+ with repeated input/head movement (possible macro)", 8);
            stationarySince.put(p.getUniqueId(), System.currentTimeMillis());
        }
    }

    private void checkMiningPattern(Player p, Deque<Material> q) {
        if (q.size() < 15) return;
        int diamonds = 0;
        for (Material m : q) if (m == Material.DIAMOND_ORE || m == Material.DEEPSLATE_DIAMOND_ORE) diamonds++;
        if (diamonds >= 15) {
            flag(p, "Unusual diamond mining pattern (15+ diamond ores in recent blocks)", 4);
            q.clear();
        }
    }

    private boolean isValuable(Material m) {
        return m == Material.DIAMOND_ORE || m == Material.DEEPSLATE_DIAMOND_ORE
                || m == Material.EMERALD_ORE || m == Material.DEEPSLATE_EMERALD_ORE
                || m == Material.ANCIENT_DEBRIS;
    }

    private void addWindowEvent(Map<UUID, Deque<Long>> map, UUID id, long window) {
        Deque<Long> q = map.computeIfAbsent(id, k -> new ArrayDeque<>());
        q.addLast(System.currentTimeMillis());
        trim(q, window);
    }

    private void trim(Deque<Long> q, long window) {
        long cutoff = System.currentTimeMillis() - window;
        while (!q.isEmpty() && q.peekFirst() < cutoff) q.removeFirst();
    }

    private void flag(Player p, String reason, int points) {
        Suspect suspect = suspects.computeIfAbsent(p.getUniqueId(), id -> new Suspect(p.getName()));
        suspect.flag(reason, points);
        plugin.getLogger().warning("Anti-cheat flag: " + p.getName() + " -> " + reason + " (score " + suspect.getScore() + ")");
    }

    public Map<UUID, Suspect> getSuspects() {
        return suspects;
    }

    public void clear(UUID uuid) {
        suspects.remove(uuid);
    }
}
