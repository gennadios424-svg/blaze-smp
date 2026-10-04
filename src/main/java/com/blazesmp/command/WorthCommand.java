package com.blazesmp.command;

import com.blazesmp.gui.WorthGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class WorthCommand implements CommandExecutor {
    private final WorthGUI gui;
    public WorthCommand(WorthGUI gui) { this.gui = gui; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Only players can use /worth."); return true; }
        if (args.length == 0) gui.open(player, "");
        else if (args.length == 1 && args[0].equalsIgnoreCase("search")) gui.openSearchPrompt(player);
        else gui.showItem(player, String.join(" ", args));
        return true;
    }
}
