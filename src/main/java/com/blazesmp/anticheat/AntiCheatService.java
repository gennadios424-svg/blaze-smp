package com.blazesmp.anticheat;

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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
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
    private final Map<UUID, Deque<Long>> clicks = new HashMap<>();
    private final Map<UUID, Deque<Long>> headMoves = new HashMap<>();
    private final Map<UUID, Integer> consecutiveDiamonds = new HashMap<>();

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
        } else {
            stationarySince.putIfAbsent(p.getUniqueId(), System.currentTimeMillis());
            if (headMoved) addWindowEvent(headMoves, p.getUniqueId(), 1000L);
            checkMacro(p);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Deque<Long> q = clicks.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<>());
        q.addLast(System.currentTimeMillis());
        trim(q, CLICK_WINDOW);
        if (q.size() >= 40) {
            flag(p, "AutoClicker (40+ clicks/sec)", 5);
            q.clear();
        }
        checkMacro(p);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player p) checkMacro(p);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Material type = event.getBlock().getType();
        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
            int count = consecutiveDiamonds.merge(p.getUniqueId(), 1, Integer::sum);
            if (count >= 15) {
                flag(p, "15+ consecutive diamond ores mined (possible X-Ray)", 6);
                consecutiveDiamonds.put(p.getUniqueId(), 0);
            }
        } else {
            consecutiveDiamonds.put(p.getUniqueId(), 0);
        }
        checkMacro(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        stationarySince.remove(id);
        clicks.remove(id);
        headMoves.remove(id);
        consecutiveDiamonds.remove(id);
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
