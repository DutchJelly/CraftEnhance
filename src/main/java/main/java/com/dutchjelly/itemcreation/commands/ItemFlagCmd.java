package main.java.com.dutchjelly.itemcreation.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import main.java.com.dutchjelly.craftenhance.commandhandling.CmdInterface;
import main.java.com.dutchjelly.craftenhance.commandhandling.CustomCmd;
import main.java.com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import main.java.com.dutchjelly.itemcreation.ItemCreator;
import main.java.com.dutchjelly.itemcreation.util.ParseResult;

@CustomCmd(cmdPath="edititem.itemflag", perms="perms.item-editor")
public class ItemFlagCmd implements CmdInterface{

	
	private CustomCmdHandler handler;
	
	public ItemFlagCmd(CustomCmdHandler handler){
		this.handler = handler;
	}
	
	@Override
	public String getDescription() {
		return "The itemflag command allows users to toggle itemflags of the held item. An example of the usage is /edititem itemflag hide_enchants hide_attributes. These itemflag names are documented in the bukkit documentation, google \"itemflags bukkit\", and the first result should contain a list of all itemflags and what they do.";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		ItemCreator creator = new ItemCreator(p.getInventory().getItemInMainHand(), args);
		ParseResult result = creator.setItemFlags();
		p.getInventory().setItemInMainHand(creator.getItem());
		handler.getMain().getMessenger().message(result.getMessage(), p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		handler.getMain().getMessenger().messageFromConfig("messages.commands.only-for-players", sender);
	}

}
