package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandRoute(cmdPath={"ceh.reload","ceh.rl"}, perms="perms.recipe-editor")
public class ReloadCmd implements ICommand {
    @Override
    public String getDescription() {
        return "Reloads CraftEnhance entirely.";
    }

    @Override
    public void handlePlayerCommand(Player p, String[] args) {
        CraftEnhance.self().reload();
        Messenger.Message("The plugin was reloaded", p);
    }

    @Override
    public void handleConsoleCommand(CommandSender sender, String[] args) {
        CraftEnhance.self().reload();
        Messenger.Message("The plugin was reloaded", sender);
    }
}
