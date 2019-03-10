package main.java.com.dutchjelly.itemcreation.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import main.java.com.dutchjelly.craftenhance.commandhandling.CmdInterface;
import main.java.com.dutchjelly.craftenhance.commandhandling.CustomCmd;
import main.java.com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import main.java.com.dutchjelly.itemcreation.ItemCreator;
import main.java.com.dutchjelly.itemcreation.util.ParseResult;

@CustomCmd(cmdPath="edititem.localizedname", perms="perms.item-editor")
public class LocalizedNameCmd implements CmdInterface{

	
	private CustomCmdHandler handler;
	
	public LocalizedNameCmd(CustomCmdHandler handler){
		this.handler = handler;
	}
	
	@Override
	public String getDescription() {
		return "The localizedname command is used to edit the name that is stored server-sided, which means that the in-game users can't see it. However, some plugins may use this itemstack property. An example of the usage is /edititem localizedname example name.";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		ItemCreator creator = new ItemCreator(p.getInventory().getItemInMainHand(), args);
		ParseResult result = creator.setLocalizedName();
		p.getInventory().setItemInMainHand(creator.getItem());
		handler.getMain().getMessenger().message(result.getMessage(), p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		handler.getMain().getMessenger().messageFromConfig("messages.commands.only-for-players", sender);
	}

}
