package com.dutchjelly.craftenhance.commandhandling;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.messaging.Debug;

public class CustomCmdHandler implements TabCompleter{
	
	
	private Map<CmdInterface, CustomCmd> commandClasses;
	//private static final String cmdPackagePath = "com.dutchjelly.craftenhance.commands";
	
	private CraftEnhance main;
	
	public CustomCmdHandler(CraftEnhance main){
		commandClasses = new HashMap<>();
		this.main = main;
		main.getDescription().getCommands().keySet().forEach(x ->{
			main.getCommand(x).setTabCompleter(this);
		});
	}
	
	public void loadCommandClasses(List<CmdInterface> baseClasses){
		if(baseClasses == null) return;
		baseClasses.forEach(x -> loadCommandClass(x));
	}
	
	public void loadCommandClass(CmdInterface baseClass){
		if(baseClass == null) return;
		CustomCmd annotation = null;
		for(Annotation annotationItem : baseClass.getClass().getAnnotations()){
			if(annotationItem instanceof CustomCmd){
				annotation = (CustomCmd) annotationItem;
				break;
			}
		}
		if(annotation == null){
			Debug.Send("Could not load the commandclass " + baseClass.getClass().getName());
			return;
		}
		commandClasses.put(baseClass, annotation);
	}
	
	public CraftEnhance getMain(){
		return main;
	}
	
	public boolean handleCommand(CommandSender sender, String label, String[] args){
		CmdInterface executor;
		args = pushLabelArg(label, args);
		
		executor = getExecutor(args);
		if(executor == null) {
			sendOptions(args, sender);
			return true;
		}
		
		CustomCmd annotation = commandClasses.get(executor);
		if(!hasPermission(sender, annotation)){
			main.getMessenger().messageFromConfig("messages.global.no-perms", sender);
			return true;
		}
		
		String[] commandLabels = getMatchingPath(args, annotation).split("\\.");
		args = popArguments(commandLabels.length, args);
		
		if(args.length > 0 && args[0].equalsIgnoreCase("help")){
			main.getMessenger().message(executor.getDescription(), sender);
			return true;
		}
		
		if(sender instanceof Player)
			executor.handlePlayerCommand((Player)sender, args);
		else
			executor.handleConsoleCommand(sender, args);
		return true;
	}
	
	private void sendOptions(String[] args, CommandSender sender){
		String[] emptyLast = new String[args.length+1];
		for(int i = 0; i < args.length; i++) emptyLast[i] = args[i];
		emptyLast[args.length] = "";
		String completions = String.join(", ", getTabCompleteMatches(emptyLast));
		if(completions.equals("")) {
			main.getMessenger().message("That is not a command.", sender);
			return;
		}
		main.getMessenger().messageFromConfig("messages.commands.show-options", sender, completions);;
	}
	
	private String[] pushLabelArg(String label, String[] args){
		String[] pushed = new String[args.length+1];
		for(int i = 1; i < pushed.length; i++)
			pushed[i] = args[i-1];
		pushed[0] = label;
		return pushed;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return this.getTabCompleteMatches(pushLabelArg(label, args));
	}
	
	private String[] popArguments(int amount, String[] args){
		if(amount == 0 || amount > args.length || args.length < 1) return args;
		String[] popped = new String[args.length-1];
		for(int i = 0; i < args.length-1; i++){
			popped[i] = args[i+1];
		}
		return popArguments(amount-1, popped);
	}
	
	public CmdInterface getExecutor(String[] args){
		int maxMatching = -1, currentMatch;
		CmdInterface bestMatch = null;
		for(CmdInterface cmdClass : commandClasses.keySet()){
			CustomCmd annotation = commandClasses.get(cmdClass);
			String matchingPath = getMatchingPath(args, annotation);
			if(matchingPath == null) continue;
			currentMatch = matchingPath.split("\\.").length;
			if(currentMatch > maxMatching){
				maxMatching = currentMatch;
				bestMatch = cmdClass;
			}
		}
		return bestMatch;
	}
	
	private String getMatchingPath(String [] args, CustomCmd annotation){
		int bestMatch = -1, currentMatch;
		String bestMatchingPath = null;
		for(String path : annotation.cmdPath()){
			currentMatch = cmdPathMatches(args, path);
			if(currentMatch > bestMatch){
				bestMatch = currentMatch;
				bestMatchingPath = path;
			}
		}
		return bestMatchingPath;
	}
	
	private int cmdPathMatches(String[] args, String path){
		String[] splitPath = path.split("\\.");
		if(splitPath.length > args.length) return -1;
		for(int i = 0; i < splitPath.length; i++)
			if(!args[i].equalsIgnoreCase(splitPath[i])) 
				return -1;
		return splitPath.length;
	}
	
	/*
	private boolean aliasMatches(String argument, String pathSection){
		
	}
	*/
	
	private boolean hasPermission(CommandSender sender, CustomCmd annotation){
		return annotation.perms().equals("") || annotation.perms() == null || sender.hasPermission(main.getConfig().getString(annotation.perms()));
	}
	
	
	private List<String> getTabCompleteMatches(String[] args){
		List<String> completions = new ArrayList<String>();
		String completion;
		for(CmdInterface cmdClass : commandClasses.keySet()){
			CustomCmd annotation = commandClasses.get(cmdClass);
			for(String path : annotation.cmdPath()){
				completion = getCompletion(args, path);
				if(completion == null) continue;
				completions.add(completion);
			}
		}
		return completions;
	}
	
	private String getCompletion(String[] args, String path){
		String[] splitPath = path.split("\\.");
		if(args.length > splitPath.length) return null;
		for(int i = 0; i < args.length-1; i++){
			if(!args[i].equalsIgnoreCase(splitPath[i])) return null;
		}
		if(!splitPath[args.length-1].toLowerCase().startsWith(args[args.length-1].toLowerCase()))
			return null;
		return splitPath[args.length-1];
	}

	
	
}
