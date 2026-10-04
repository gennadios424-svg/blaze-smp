package com.blazesmp;

import com.blazesmp.economy.MoneyService;
import com.blazesmp.economy.WorthService;
import com.blazesmp.gui.SellGUI;
import com.blazesmp.gui.WorthGUI;
import com.blazesmp.command.BalanceCommand;
import com.blazesmp.command.EcoCommand;
import com.blazesmp.command.SellCommand;
import com.blazesmp.command.WorthCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlazeSMP extends JavaPlugin {
    private MoneyService moneyService;
    private WorthService worthService;
    private SellGUI sellGUI;
    private WorthGUI worthGUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        moneyService = new MoneyService(this);
        worthService = new WorthService(this);
        worthService.reload();
        sellGUI = new SellGUI(this, worthService, moneyService);
        worthGUI = new WorthGUI(this, worthService);

        register("worth", new WorthCommand(worthGUI));
        register("sell", new SellCommand(sellGUI));
        register("balance", new BalanceCommand(moneyService));
        register("eco", new EcoCommand(moneyService));
        getServer().getPluginManager().registerEvents(sellGUI, this);
        getServer().getPluginManager().registerEvents(worthGUI, this);
        getLogger().info("Blaze SMP economy enabled.");
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
