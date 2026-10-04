package com.blazesmp.command;

import com.blazesmp.anticheat.AntiCheatService;
import com.blazesmp.gui.SusListGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SusListCommand implements CommandExecutor {
    private final AntiCheatService antiCheat;
    private final SusListGUI gui;

    public SusListCommand(AntiCheatService antiCheat, SusListGUI gui) {
        this.antiCheat = antiCheat;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("blazesmp.suslist")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command must be used in-game.");
            return true;
        }
        antiCheat.cleanupExpired();
        gui.open(player);
        return true;
    }
}
