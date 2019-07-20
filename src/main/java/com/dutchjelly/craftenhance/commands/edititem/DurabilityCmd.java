package com.dutchjelly.craftenhance.commands.edititem;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dutchjelly.craftenhance.commandhandling.CmdInterface;
import com.dutchjelly.craftenhance.commandhandling.CustomCmd;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.itemcreation.ItemCreator;
import com.dutchjelly.craftenhance.itemcreation.ParseResult;

@CustomCmd(cmdPath="edititem.durability", perms="perms.item-editor")
public class DurabilityCmd implements CmdInterface{

	
	private CustomCmdHandler handler;
	
	public DurabilityCmd(CustomCmdHandler handler){
		this.handler = handler;
	}
	
	@Override
	public String getDescription() {
		return "The durability command allows players to edit the durability of items in their main hand. An example of the usage is \"/edititem durability 50\". This will set the durability of the item in the command sender to 50%.";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		ItemCreator creator = new ItemCreator(p.getInventory().getItemInHand(), args);
		ParseResult result = creator.setDurability();
		p.getInventory().setItemInHand(creator.getItem());
		handler.getMain().getMessenger().message(result.getMessage(), p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		handler.getMain().getMessenger().messageFromConfig("messages.commands.only-for-players", sender);
	}

}
