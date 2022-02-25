package com.dutchjelly.craftenhance.commandhandling;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.dutchjelly.craftenhance.messaging.Debug;

public class CustomCmdHandler implements TabCompleter{
	
	
	private Map<ICommand, CommandRoute> commandClasses;
	//private static final String cmdPackagePath = "com.dutchjelly.craftenhance.commands";
	
	private CraftEnhance main;
	
	//Pass main instance and register tab completion on all commands.
	public CustomCmdHandler(CraftEnhance main){
		commandClasses = new HashMap<>();
		this.main = main;
		main.getDescription().getCommands().keySet().forEach(x ->{
			main.getCommand(x).setTabCompleter(this);
		});
	}
	
	//Load all command classes in baseClasses so they can handle commands.
	public void loadCommandClasses(List<ICommand> baseClasses){
		if(baseClasses == null) return;
		baseClasses.forEach(x -> loadCommandClass(x));
	}
	
	//Load the command class baseClass by finding the annotation and 
	//storing it in the commandClasses map.
	public void loadCommandClass(ICommand baseClass){
		if(baseClass == null) return;
		CommandRoute annotation = null;
		for(Annotation annotationItem : baseClass.getClass().getAnnotations()){
			if(annotationItem instanceof CommandRoute){
				annotation = (CommandRoute) annotationItem;
				break;
			}
		}
		if(annotation == null){
			Debug.Send("Could not load the commandclass " + baseClass.getClass().getName());
			return;
		}
		commandClasses.put(baseClass, annotation);
	}
	
	
	//Get the main plugin instance.
	public CraftEnhance getMain(){
		return main;
	}
	
	//Assign command to a class and check for permissions.
	public boolean handleCommand(CommandSender sender, String label, String[] args){
		ICommand executor;
		args = pushLabelArg(label, args);
		
		executor = getExecutor(args);
		if(executor == null) {
			sendOptions(args, sender);
			return true;
		}
		
		CommandRoute annotation = commandClasses.get(executor);
		if(!hasPermission(sender, annotation)){
			Messenger.MessageFromConfig("messages.global.no-perms", sender);
			return true;
		}
		
		String[] commandLabels = getMatchingPath(args, annotation).split("\\.");
		args = popArguments(commandLabels.length, args);
		
		if(args.length > 0 && args[0].equalsIgnoreCase("help")){
			Messenger.Message(executor.getDescription(), sender);
			return true;
		}
		
		if(sender instanceof Player)
			executor.handlePlayerCommand((Player)sender, args);
		else
			executor.handleConsoleCommand(sender, args);
		return true;
	}
	
	//Send options that could complete the given arguments to sender.
	private void sendOptions(String[] args, CommandSender sender){
		String[] emptyLast = new String[args.length+1];
		for(int i = 0; i < args.length; i++) {
			emptyLast[i] = args[i];
		}
		emptyLast[args.length] = "";
		String completions = String.join(", ", getTabCompleteMatches(emptyLast));
		if(completions.equals("")) {
			Messenger.Message("That is not a command.", sender);
			return;
		}
		Messenger.MessageFromConfig("messages.commands.show-options", sender, completions);;
	}
	
	//Push the label argument to index 0 of the array of arguments.
	private String[] pushLabelArg(String label, String[] args){
		String[] pushed = new String[args.length+1];
		for(int i = 1; i < pushed.length; i++)
			pushed[i] = args[i-1];
		pushed[0] = label;
		return pushed;
	}
	
	//Handle tab completion by returning all tab completions.
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		String[] completeArgs = pushLabelArg(label, args);
		List<String> tabCompletion = new ArrayList<>();
//		ICommand suitableExecutor = getExecutor(args);
//		if(suitableExecutor != null && suitableExecutor instanceof ICompletionProvider)
//			tabCompletion.addAll(((ICompletionProvider)suitableExecutor).getCompletions(args));
		tabCompletion.addAll(getTabCompleteMatches(pushLabelArg(label, args)));
		return tabCompletion;
	}
	
	//Pop amount arguments recursively from args.
	private String[] popArguments(int amount, String[] args){
		if(amount == 0 || amount > args.length || args.length < 1) return args;
		String[] popped = new String[args.length-1];
		for(int i = 0; i < args.length-1; i++){
			popped[i] = args[i+1];
		}
		return popArguments(amount-1, popped);
	}
	
	//Get the executor object from given args.
	public ICommand getExecutor(String[] args){
		int maxMatching = -1, currentMatch;
		ICommand bestMatch = null;
		for(ICommand cmdClass : commandClasses.keySet()){
			CommandRoute annotation = commandClasses.get(cmdClass);
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
	
	//Get the best matching path with args of all paths in the annotation.
	private String getMatchingPath(String [] args, CommandRoute annotation){
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
	
	//Check if the array of strings match with the path by returning the length
	//of the found path. Returns -1 if it doesn't match.
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
	
	//Returns if the sender has permission for command class with annotation.
	private boolean hasPermission(CommandSender sender, CommandRoute annotation){
		if(annotation.perms().equals("") || annotation.perms() == null)
			return true;
		String perms = main.getConfig().getString(annotation.perms()) + "";
		if(perms.trim() == "") return true;
		return sender.hasPermission(perms);
	}
	
	//Find all possible tab completions for args.
	private List<String> getTabCompleteMatches(String[] args){
		List<String> completions = new ArrayList<String>();
		String completion;
		for(ICommand cmdClass : commandClasses.keySet()){
			CommandRoute annotation = commandClasses.get(cmdClass);
			for(String path : annotation.cmdPath()){
				completion = getCompletion(args, path);
				if(completion == null) continue;
				completions.add(completion);
			}
		}
		return completions;
	}
	
	//Returns the possible completion of args to result in path. So returns null
	//if there is none.
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
