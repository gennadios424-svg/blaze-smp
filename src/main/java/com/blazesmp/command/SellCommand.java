package com.blazesmp.command;

import com.blazesmp.gui.SellGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SellCommand implements CommandExecutor {
    private final SellGUI gui;
    public SellCommand(SellGUI gui) { this.gui = gui; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Only players can use /sell."); return true; }
        gui.open(player);
        return true;
    }
}
