package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.commandhandling.CmdInterface;
import com.dutchjelly.craftenhance.commandhandling.CustomCmd;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CustomCmd(cmdPath="ceh.createrecipe", perms="perms.recipe-editor")
public class CreateRecipeCmd implements CmdInterface {

	private CustomCmdHandler handler;
	
	public CreateRecipeCmd(CustomCmdHandler handler){
		this.handler = handler;
	}
	
	@Override
	public String getDescription() {
		return "The createrecipe command allows users to create a recipe and open the editor of it. The usage is /ceh createrecipe [key] [permission]. You can leave both parameters empty. However, if you do want to customise recipe keys and permissions: the key has to be unique, and the permission can be empty to not have any permission. An example: /ceh createrecipe army_chest ceh.army-chest. The now created recipe has a key of army_chest and a permission of ceh.army-chest.";
	}
	@Override
	public void handlePlayerCommand(Player p, String[] args) {

        //Fill in a unique key and empty permission for the user.
	    if(args.length == 0){
	        int uniqueKeyIndex = 1;
	        while(!handler.getMain().getFileManager().isUniqueRecipeKey("recipe" + uniqueKeyIndex))
                uniqueKeyIndex++;
            handler.getMain().getGUIContainer().openRecipeCreator("recipe" + uniqueKeyIndex, "", p);
	        return;
        }

        //Use the input that the user gave.
		if(args.length == 1){
			args = addEmptyString(args);
		}
		else if(args.length != 2){
			handler.getMain().getMessenger().messageFromConfig("messages.commands.few-arguments", p, "2");
			return;
		}
		
		if(!handler.getMain().getFileManager().isUniqueRecipeKey(args[0])){
			handler.getMain().getMessenger().message("The specified recipe key isn't unique.", p);
			return;
		}
		handler.getMain().getGUIContainer().openRecipeCreator(args[0], args[1], p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		handler.getMain().getMessenger().messageFromConfig("messages.commands.only-for-players", sender);
	}
	
	//Create a new array object so return that.
	private String[] addEmptyString(String[] args){
		String[] newArray = new String[args.length + 1];
		newArray[0] = args[0];
		newArray[1] = "";
		return newArray;
	}
}
