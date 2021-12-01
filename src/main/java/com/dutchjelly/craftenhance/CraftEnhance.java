package com.dutchjelly.craftenhance;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.api.CraftEnhanceAPI;
import com.dutchjelly.craftenhance.commands.ceh.*;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.crafthandling.customcraftevents.ExecuteCommand;
import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.files.GuiTemplatesFile;
import com.dutchjelly.craftenhance.gui.guis.CustomCraftingTable;
import com.dutchjelly.craftenhance.gui.guis.viewers.WBRecipeViewer;
import com.dutchjelly.craftenhance.updatechecking.VersionChecker;
import com.dutchjelly.craftenhance.util.Metrics;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.crafthandling.RecipeInjector;
import com.dutchjelly.craftenhance.files.ConfigFormatter;
import com.dutchjelly.craftenhance.files.FileManager;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.dutchjelly.craftenhance.commands.edititem.DisplayNameCmd;
import com.dutchjelly.craftenhance.commands.edititem.DurabilityCmd;
import com.dutchjelly.craftenhance.commands.edititem.EnchantCmd;
import com.dutchjelly.craftenhance.commands.edititem.ItemFlagCmd;
import com.dutchjelly.craftenhance.commands.edititem.LocalizedNameCmd;
import com.dutchjelly.craftenhance.commands.edititem.LoreCmd;

public class CraftEnhance extends JavaPlugin{

    private static CraftEnhance plugin;
	public static CraftEnhance self(){
	    return plugin;
    }

    @Getter
	private FileManager fm;

	@Getter
	private GuiManager guiManager;

	@Getter
	private GuiTemplatesFile guiTemplatesFile;

	private CustomCmdHandler commandHandler;

	private RecipeInjector injector;

	@Getter
	private boolean usingItemsAdder;

	@Override
	public void onEnable(){
	    plugin = this;
		//The file manager needs serialization, so firstly register the classes.
		registerSerialization();

		saveDefaultConfig();

		Debug.init(this);
		Debug.Send("Checking for config updates.");
		File configFile = new File(getDataFolder(), "config.yml");
		FileManager.EnsureResourceUpdate("config.yml", configFile, YamlConfiguration.loadConfiguration(configFile), this);

		Debug.Send("Coloring config messages.");
		ConfigFormatter.init(this).formatConfigMessages();
		Messenger.Init(this);

		ItemMatchers.init(getConfig().getBoolean("enable-backwards-compatible-item-matching"));

		this.usingItemsAdder = this.getServer().getPluginManager().getPlugin("ItemsAdder") != null;

		//Most other instances use the file manager, so setup before everything.
        Debug.Send("Setting up the file manager for recipes.");
		setupFileManager();

        Debug.Send("Loading recipes");
        RecipeLoader loader = RecipeLoader.getInstance();
		fm.getRecipes().stream().filter(x -> x.validate() == null).forEach(loader::loadRecipe);
		loader.printGroupsDebugInfo();
		loader.disableServerRecipes(
		        fm.readDisabledServerRecipes().stream().map(x ->
                        Adapter.FilterRecipes(loader.getServerRecipes(), x)
                ).collect(Collectors.toList())
        );

		injector = new RecipeInjector(this);
		injector.registerContainerOwners(fm.getContainerOwners());

		Debug.Send("Loading gui templates");
		guiTemplatesFile = new GuiTemplatesFile(this);
		guiTemplatesFile.load();

		guiManager = new GuiManager(this);

		Debug.Send("Setting up listeners and commands");
		setupListeners();
		setupCommands();

		Messenger.Message("CraftEnhance is managed and developed by DutchJelly.");
		Messenger.Message("If you find a bug in the plugin, please report it to https://dev.bukkit.org/projects/craftenhance.");
		VersionChecker checker = VersionChecker.init(this);
		if(!checker.runVersionCheck()){
			for(int i = 0; i < 4; i++)
		    	Messenger.Message("WARN: The installed version isn't tested to work with the game version of the server.");
        }
		checker.runUpdateCheck();


		final int metricsId = 9023;
		new Metrics(this, metricsId);


		CraftEnhanceAPI.registerListener(new ExecuteCommand());
	}
	
	@Override
	public void onDisable(){
		Debug.Send("Saving container owners...");
		fm.saveContainerOwners(injector.getContainerOwners());
		Debug.Send("Saving disabled recipes...");
        fm.saveDisabledServerRecipes(RecipeLoader.getInstance().getDisabledServerRecipes().stream().map(x -> Adapter.GetRecipeIdentifier(x)).collect(Collectors.toList()));
        getServer().resetRecipes();
	}

	public void reload(){
		onDisable();
		RecipeLoader.clearInstance();
		reloadConfig();
		onEnable();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		//Make sure that the user doesn't get a whole stacktrace when using an unsupported server jar.
		//Note that this error could only get caused by onEnable() not being called.
		if(commandHandler == null){
			Messenger.Message("Could not execute the command.", sender);
			Messenger.Message("Something went wrong with initializing the commandHandler. Please make sure to use" +
			" Spigot or Bukkit when using this plugin. If you are using Spigot or Bukkit and still experiencing this " +
			"issue, please send a bug report here: https://dev.bukkit.org/projects/craftenhance.");
			Messenger.Message("Disabling the plugin...");
			getPluginLoader().disablePlugin(this);
		}

		commandHandler.handleCommand(sender, label, args);
		return true;
	}
	
	//Registers the classes that extend ConfigurationSerializable.
	private void registerSerialization(){
		ConfigurationSerialization.registerClass(WBRecipe.class, "EnhancedRecipe");
        ConfigurationSerialization.registerClass(WBRecipe.class, "Recipe");
		ConfigurationSerialization.registerClass(FurnaceRecipe.class, "FurnaceRecipe");
	}
	
	//Assigns executor classes for the commands.
	private void setupCommands() {
        commandHandler = new CustomCmdHandler(this);
        //All commands with the base /edititem
        commandHandler.loadCommandClasses(Arrays.asList(
                new DisplayNameCmd(commandHandler),
                new DurabilityCmd(commandHandler),
                new EnchantCmd(commandHandler),
                new ItemFlagCmd(commandHandler),
                new LocalizedNameCmd(commandHandler),
                new LoreCmd(commandHandler))
        );
        //All command with the base /ceh
        commandHandler.loadCommandClasses(Arrays.asList(
                new CreateRecipeCmd(commandHandler),
                new RecipesCmd(commandHandler),
                new SpecsCommand(commandHandler),
                new ChangeKeyCmd(commandHandler),
                new CleanItemFileCmd(commandHandler),
                new SetPermissionCmd(commandHandler),
                new ReloadCmd(),
                new ViewCmd(commandHandler),
                new Disabler(commandHandler))
        );
    }
	
	//Registers the listener class to the server.
	private void setupListeners(){
		getServer().getPluginManager().registerEvents(injector, this);
		getServer().getPluginManager().registerEvents(guiManager, this);
		getServer().getPluginManager().registerEvents(RecipeLoader.getInstance(), this);
	}
	
	private void setupFileManager(){
		fm = FileManager.init(this);
		fm.cacheItems();
		fm.cacheRecipes();
	}

	public void openEnhancedCraftingTable(Player p){
		CustomCraftingTable table = new CustomCraftingTable(
				getGuiManager(),
				getGuiTemplatesFile().getTemplate(WBRecipeViewer.class),
				null, p
		);
		getGuiManager().openGUI(p, table);
	}
	
}
