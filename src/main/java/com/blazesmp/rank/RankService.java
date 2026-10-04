package com.blazesmp.rank;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.bukkit.scoreboard.Team.DisplaySlot;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RankService {
    private final JavaPlugin plugin;
    private final File file;
    private final Map<UUID, Rank> ranks = new HashMap<>();
    private final Map<UUID, org.bukkit.permissions.PermissionAttachment> attachments = new HashMap<>();

    public RankService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ranks.yml");
        load();
    }

    public synchronized Rank getRank(OfflinePlayer player) {
        return ranks.getOrDefault(player.getUniqueId(), Rank.MEMBER);
    }

    public synchronized void setRank(OfflinePlayer player, Rank rank) {
        ranks.put(player.getUniqueId(), rank == null ? Rank.MEMBER : rank);
        save();
        if (player.isOnline()) apply((Player) player);
    }

    public synchronized void apply(Player player) {
        Rank rank = getRank(player);
        org.bukkit.permissions.PermissionAttachment old = attachments.remove(player.getUniqueId());
        if (old != null) player.removeAttachment(old);

        org.bukkit.permissions.PermissionAttachment attachment = player.addAttachment(plugin);
        for (String permission : plugin.getConfig().getStringList("rank-permissions." + rank.name().toLowerCase())) {
            attachment.setPermission(permission, true);
        }
        attachments.put(player.getUniqueId(), attachment);

        updateTab(player, rank);
        updateScoreboardTeam(player, rank);
    }

    public synchronized void remove(Player player) {
        org.bukkit.permissions.PermissionAttachment attachment = attachments.remove(player.getUniqueId());
        if (attachment != null) player.removeAttachment(attachment);
    }

    private void updateTab(Player player, Rank rank) {
        player.setPlayerListName("§" + colorCode(rank) + "[" + rank.displayName() + "] §f" + player.getName());
    }

    private char colorCode(Rank rank) {
        return switch (rank) {
            case OWNER -> 'e';
            case DEV -> '6';
            case MOD -> 'c';
            case MEDIA -> 'd';
            case MEMBER -> '7';
        };
    }

    private void updateScoreboardTeam(Player player, Rank rank) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "blaze_" + rank.name().toLowerCase();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) team = scoreboard.registerNewTeam(teamName);
        team.setPrefix("§" + colorCode(rank) + "[" + rank.displayName() + "] §f");
        team.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.ALWAYS);
        for (Team other : scoreboard.getTeams()) {
            if (other != team) other.removeEntry(player.getName());
        }
        team.addEntry(player.getName());
    }

    public synchronized void save() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) return;
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, Rank> entry : ranks.entrySet()) {
            yaml.set("ranks." + entry.getKey(), entry.getValue().name());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save ranks: " + e.getMessage());
        }
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (yaml.getConfigurationSection("ranks") == null) return;
        for (String key : yaml.getConfigurationSection("ranks").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                Rank rank = Rank.parse(yaml.getString("ranks." + key, "MEMBER"));
                ranks.put(uuid, rank == null ? Rank.MEMBER : rank);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}
