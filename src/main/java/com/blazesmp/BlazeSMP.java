package com.blazesmp;

import com.blazesmp.economy.MoneyService;
import com.blazesmp.economy.WorthService;
import com.blazesmp.gui.SellGUI;
import com.blazesmp.gui.WorthGUI;
import com.blazesmp.command.BalanceCommand;
import com.blazesmp.command.EcoCommand;
import com.blazesmp.command.SellCommand;
import com.blazesmp.command.WorthCommand;
import com.blazesmp.command.DrillCommand;
import com.blazesmp.command.ShardGainerCommand;
import com.blazesmp.custom.DrillListener;
import com.blazesmp.custom.DrillService;
import com.blazesmp.custom.ShardGainerListener;
import com.blazesmp.custom.ShardGainerService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlazeSMP extends JavaPlugin {
    private MoneyService moneyService;
    private WorthService worthService;
    private SellGUI sellGUI;
    private WorthGUI worthGUI;
    private DrillService drillService;
    private ShardGainerService shardGainerService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        moneyService = new MoneyService(this);
        worthService = new WorthService(this);
        worthService.reload();
        sellGUI = new SellGUI(this, worthService, moneyService);
        worthGUI = new WorthGUI(this, worthService);
        drillService = new DrillService(this);
        shardGainerService = new ShardGainerService(this);

        register("worth", new WorthCommand(worthGUI));
        register("sell", new SellCommand(sellGUI));
        register("balance", new BalanceCommand(moneyService));
        register("eco", new EcoCommand(moneyService));
        register("drill", new DrillCommand(drillService));
        register("shardgainer", new ShardGainerCommand(shardGainerService));

        getServer().getPluginManager().registerEvents(sellGUI, this);
        getServer().getPluginManager().registerEvents(worthGUI, this);
        getServer().getPluginManager().registerEvents(new DrillListener(drillService), this);
        getServer().getPluginManager().registerEvents(new ShardGainerListener(shardGainerService), this);
        getLogger().info("Blaze SMP economy, Drill and Shardgainer systems enabled.");
    }

    @Override
    public void onDisable() {
        if (sellGUI != null) sellGUI.closeAllAndReturnItems();
        if (moneyService != null) moneyService.save();
    }

    private void register(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) command.setExecutor(executor);
    }

    public MoneyService getMoneyService() { return moneyService; }
    public WorthService getWorthService() { return worthService; }
}
