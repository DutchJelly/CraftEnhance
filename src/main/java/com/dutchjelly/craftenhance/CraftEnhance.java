package com.dutchjelly.craftenhance;

import java.util.Arrays;

import com.dutchjelly.craftenhance.updatechecking.VersionChecker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.commands.ceh.ChangeKeyCmd;
import com.dutchjelly.craftenhance.commands.ceh.CleanItemFileCmd;
import com.dutchjelly.craftenhance.commands.ceh.CreateRecipeCmd;
import com.dutchjelly.craftenhance.commands.ceh.OrderRecipesCmd;
import com.dutchjelly.craftenhance.commands.ceh.RecipesCmd;
import com.dutchjelly.craftenhance.commands.ceh.SetPermissionCmd;
import com.dutchjelly.craftenhance.commands.ceh.SpecsCommand;
import com.dutchjelly.craftenhance.crafthandling.RecipeInjector;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.files.ConfigFormatter;
import com.dutchjelly.craftenhance.files.FileManager;
import com.dutchjelly.craftenhance.gui.GUIContainer;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.dutchjelly.craftenhance.Util.GUIButtons;
import com.dutchjelly.craftenhance.model.CraftRecipe;
import com.dutchjelly.craftenhance.commands.edititem.DisplayNameCmd;
import com.dutchjelly.craftenhance.commands.edititem.DurabilityCmd;
import com.dutchjelly.craftenhance.commands.edititem.EnchantCmd;
import com.dutchjelly.craftenhance.commands.edititem.ItemFlagCmd;
import com.dutchjelly.craftenhance.commands.edititem.LocalizedNameCmd;
import com.dutchjelly.craftenhance.commands.edititem.LoreCmd;

public class CraftEnhance extends JavaPlugin{

	//TODO Try to add categories.
	//TODO Clean up redundant setcancelled in GUIs onclick event.
	
	private FileManager fm;
	private RecipeLoader loader;
	private GUIContainer guiContainer;
	private RecipeInjector injector;
	private CustomCmdHandler commandHandler;
	private Messenger messenger;
	
	@Override
	public void onEnable(){
		
		//The filemanager needs serialization, so firstly register the classes.
		registerSerialization();

		saveDefaultConfig();
		Debug.init(this);
		//Most other instances use the filemanager, so setup before everything.
		setupFileManager();
		
		GUIButtons.init();
		ConfigFormatter.init(this).formatConfigMessages();
		createInstances();
		loader.loadRecipes();
		setupListeners();
		setupCommands();

		getMessenger().message("CraftEnhance is managed and developed by DutchJelly.");
		getMessenger().message("If you find a bug in the plugin, please report it to https://dev.bukkit.org/projects/craftenhance.");
		VersionChecker.init(this).runVersionCheck();
	}
	
	@Override
	public void onDisable(){
		guiContainer.closeAll();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

		//Make sure that the user doesn't get a whole stacktrace when using an unsupported server jar.
		//Note that this error could only get caused by onEnable() not being called.
		if(commandHandler == null){
			getMessenger().message("Could not execute the command.", sender);
			getMessenger().message("Something went wrong with initializing the commandHandler. Please make sure to use" +
			" Spigot or Bukkit when using this plugin. If you are using Spigot or Bukkit and still experiencing this " +
			"issue, please send a bug report here: https://dev.bukkit.org/projects/craftenhance.");
			getMessenger().message("Disabling the plugin...");
			getPluginLoader().disablePlugin(this);
		}

		commandHandler.handleCommand(sender, label, args);
		return true;
	}
	
	//Registers the classes that extend ConfigurationSerializable.
	private void registerSerialization(){
		ConfigurationSerialization.registerClass(CraftRecipe.class, "Recipe");
	}
	
	//Create basic instances where order doesn't matter.
	private void createInstances(){
		loader = new RecipeLoader(this);
		guiContainer = new GUIContainer(this);
		injector = new RecipeInjector(fm);
		messenger = new Messenger(this);
	}
	
	//Assigns executor classes for the commands.
	private void setupCommands(){
		commandHandler = new CustomCmdHandler(this);
		//All commands with the base /edititem
		commandHandler.loadCommandClasses(Arrays.asList(new DisplayNameCmd(commandHandler), new DurabilityCmd(commandHandler),
				new EnchantCmd(commandHandler), new ItemFlagCmd(commandHandler), new LocalizedNameCmd(commandHandler), 
				new LoreCmd(commandHandler)));
		//All command with the base /ceh
		commandHandler.loadCommandClasses(Arrays.asList(new CreateRecipeCmd(commandHandler), new OrderRecipesCmd(commandHandler),
				new RecipesCmd(commandHandler), new SpecsCommand(commandHandler), new ChangeKeyCmd(commandHandler), 
				new CleanItemFileCmd(commandHandler), new SetPermissionCmd(commandHandler)));
		
	}
	
	//Registers the listener class to the server.
	private void setupListeners(){
		getServer().getPluginManager().registerEvents(new EventClass(this), this);
	}
	
	private void setupFileManager(){
		fm = FileManager.init(this);
		fm.cacheItems();
		fm.cacheRecipes();
	}
	
	public FileManager getFileManager(){
		return fm;
	}
	public RecipeLoader getRecipeLoader(){
		return loader;
	}
	public GUIContainer getGUIContainer(){
		return guiContainer;
	}
	public RecipeInjector getRecipeInjector(){
		return injector;
	}
	public Messenger getMessenger(){
		return messenger;
	}
	
	
}
