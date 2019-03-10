package main.java.com.dutchjelly.itemcreation.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import main.java.com.dutchjelly.craftenhance.commandhandling.CmdInterface;
import main.java.com.dutchjelly.craftenhance.commandhandling.CustomCmd;
import main.java.com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import main.java.com.dutchjelly.itemcreation.ItemCreator;
import main.java.com.dutchjelly.itemcreation.util.ParseResult;

@CustomCmd(cmdPath="edititem.lore", perms="perms.item-editor")
public class LoreCmd implements CmdInterface{

	
	private CustomCmdHandler handler;
	
	public LoreCmd(CustomCmdHandler handler){
		this.handler = handler;
	}
	
	@Override
	public String getDescription() {
		return "The lore command allows users to edit the lore of the item. The lore is basically the description below the item name and attributes when hovering over it. An example of the usage is /edititem lore 3 this example text will be put on line 3 of the lore of the held item.";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		ItemCreator creator = new ItemCreator(p.getInventory().getItemInMainHand(), args);
		ParseResult result = creator.setLore();
		p.getInventory().setItemInMainHand(creator.getItem());
		handler.getMain().getMessenger().message(result.getMessage(), p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		handler.getMain().getMessenger().messageFromConfig("messages.commands.only-for-players", sender);
	}

}
