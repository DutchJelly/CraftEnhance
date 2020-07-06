package com.dutchjelly.craftenhance.commands.edititem;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.commandhandling.ICompletionProvider;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.sun.tools.javac.code.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.itemcreation.ItemCreator;
import com.dutchjelly.craftenhance.itemcreation.ParseResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandRoute(cmdPath="edititem.enchant", perms="perms.item-editor")
public class EnchantCmd implements ICommand, ICompletionProvider {

	
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
		ItemCreator creator = new ItemCreator(p.getInventory().getItemInHand(), args);
		ParseResult result = creator.enchant();
		p.getInventory().setItemInHand(creator.getItem());
		Messenger.Message(result.getMessage(), p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		Messenger.MessageFromConfig("messages.commands.only-for-players", sender);
	}

	@Override
	public List<String> getCompletions(String[] args) {
		if(args == null)
			args = new String[0];
		//ceh enchant unbreaki[tab]
		boolean provideEnchantment = (args.length % 2) != 0;
		String toComplete = args[args.length-1];
		if(!provideEnchantment){
            return Arrays.asList("1","2","3","4","5");
        }
        List<Enchantment> enchants = Arrays.asList(Enchantment.values());
        List<String> completions = new ArrayList<>();
        enchants.stream().filter(x ->
                x.getName().toLowerCase().startsWith(toComplete.toLowerCase()))
                .collect(Collectors.toList())
                .forEach(x -> completions.add(x.getName()));
        return completions;
	}
}

