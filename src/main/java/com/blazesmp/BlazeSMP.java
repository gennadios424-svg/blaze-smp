package com.blazesmp;

import com.blazesmp.economy.MoneyService;
import com.blazesmp.economy.ShardService;
import com.blazesmp.economy.WorthService;
import com.blazesmp.gui.SellGUI;
import com.blazesmp.gui.WorthGUI;
import com.blazesmp.command.BalanceCommand;
import com.blazesmp.command.EcoCommand;
import com.blazesmp.command.SellCommand;
import com.blazesmp.command.WorthCommand;
import com.blazesmp.command.DrillCommand;
import com.blazesmp.command.ShardGainerCommand;
import com.blazesmp.command.ShardCommand;
import com.blazesmp.command.RankCommand;
import com.blazesmp.custom.DrillListener;
import com.blazesmp.custom.DrillService;
import com.blazesmp.custom.ShardGainerListener;
import com.blazesmp.custom.ShardGainerService;
import com.blazesmp.rank.RankListener;
import com.blazesmp.rank.RankService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlazeSMP extends JavaPlugin {
    private MoneyService moneyService;
    private ShardService shardService;
    private WorthService worthService;
    private SellGUI sellGUI;
    private WorthGUI worthGUI;
    private DrillService drillService;
    private ShardGainerService shardGainerService;
    private RankService rankService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        moneyService = new MoneyService(this);
        shardService = new ShardService(this);
        rankService = new RankService(this);

        worthService = new WorthService(this);
        worthService.reload();
        sellGUI = new SellGUI(this, worthService, moneyService);
        worthGUI = new WorthGUI(this, worthService);
        drillService = new DrillService(this);
        shardGainerService = new ShardGainerService(this, shardService);

        register("worth", new WorthCommand(worthGUI));
        register("sell", new SellCommand(sellGUI));
        register("balance", new BalanceCommand(moneyService));
        register("eco", new EcoCommand(moneyService));
        register("drill", new DrillCommand(drillService));
        register("shardgainer", new ShardGainerCommand(shardGainerService));
        register("shards", new ShardCommand(shardService));
        register("rank", new RankCommand(rankService));

        getServer().getPluginManager().registerEvents(sellGUI, this);
        getServer().getPluginManager().registerEvents(worthGUI, this);
        getServer().getPluginManager().registerEvents(new DrillListener(drillService), this);
        getServer().getPluginManager().registerEvents(new ShardGainerListener(shardGainerService), this);
        getServer().getPluginManager().registerEvents(new RankListener(rankService), this);

        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            rankService.apply(player);
        }

        getLogger().info("Blaze SMP economy, Drill, Shardgainer, Emerald Shards and Rank systems enabled.");
    }

    @Override
    public void onDisable() {
        if (sellGUI != null) sellGUI.closeAllAndReturnItems();
        if (moneyService != null) moneyService.save();
        if (shardService != null) shardService.save();
        if (rankService != null) rankService.save();
    }

    private void register(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) command.setExecutor(executor);
    }

    public MoneyService getMoneyService() { return moneyService; }
    public ShardService getShardService() { return shardService; }
    public WorthService getWorthService() { return worthService; }
    public RankService getRankService() { return rankService; }
}
