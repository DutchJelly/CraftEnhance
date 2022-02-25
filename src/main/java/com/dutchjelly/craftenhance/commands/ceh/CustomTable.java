package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandRoute(cmdPath={"ceh.customtable"}, perms="perms.recipe-viewer")
public class CustomTable implements ICommand {
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void handlePlayerCommand(Player p, String[] args) {
        CraftEnhance.self().openEnhancedCraftingTable(p);
    }

    @Override
    public void handleConsoleCommand(CommandSender sender, String[] args) {

    }
}
