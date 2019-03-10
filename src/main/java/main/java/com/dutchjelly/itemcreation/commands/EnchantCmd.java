package main.java.com.dutchjelly.itemcreation.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import main.java.com.dutchjelly.craftenhance.commandhandling.CmdInterface;
import main.java.com.dutchjelly.craftenhance.commandhandling.CustomCmd;
import main.java.com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import main.java.com.dutchjelly.itemcreation.ItemCreator;
import main.java.com.dutchjelly.itemcreation.util.ParseResult;

@CustomCmd(cmdPath="edititem.enchant", perms="perms.item-editor")
public class EnchantCmd implements CmdInterface{

	
	private CustomCmdHandler handler;
	
	public EnchantCmd(CustomCmdHandler handler){
		this.handler = handler;
	}
	
	@Override
	public String getDescription() {
		return "The enchant command allows users to efficiently enchant items with any enchantment with any level. On use this command will remove all enchantments currently on the held item and enchant it with all specified enchantments. An example of the usage is /edititem enchant protection 10 punch 10 unbreaking 3 fire_protection 4.";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		ItemCreator creator = new ItemCreator(p.getInventory().getItemInMainHand(), args);
		ParseResult result = creator.enchant();
		p.getInventory().setItemInMainHand(creator.getItem());
		handler.getMain().getMessenger().message(result.getMessage(), p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		handler.getMain().getMessenger().messageFromConfig("messages.commands.only-for-players", sender);
	}

}

