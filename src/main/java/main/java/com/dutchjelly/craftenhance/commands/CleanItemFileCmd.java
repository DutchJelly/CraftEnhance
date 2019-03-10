package main.java.com.dutchjelly.craftenhance.commands;

import main.java.com.dutchjelly.craftenhance.commandhandling.CmdInterface;
import main.java.com.dutchjelly.craftenhance.commandhandling.CustomCmd;
import main.java.com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CustomCmd(cmdPath="ceh.cleanitemfile", perms="perms.recipe-editor")
public class CleanItemFileCmd implements CmdInterface {

	private CustomCmdHandler handler;
	
	public CleanItemFileCmd(CustomCmdHandler handler){
		this.handler = handler;
	}

	@Override
	public String getDescription() {
		return "Removes the unused items from the config. The usage is /ceh cleanitemfile.";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		handler.getMain().getFileManager().cleanItemFile();
		handler.getMain().getMessenger().message("Successfully cleared all unused items.", p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		handler.getMain().getMessenger().messageFromConfig("messages.commands.only-for-players", sender);
	}
}
