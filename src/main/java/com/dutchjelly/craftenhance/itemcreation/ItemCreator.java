package com.dutchjelly.craftenhance.itemcreation;

import java.util.ArrayList;
import java.util.List;

import com.dutchjelly.bukkitadapter.Adapter;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemCreator {
	
	private ItemStack item;
	private String[] args;


	public ItemCreator(ItemStack item, String[] args){
		this.item = item;
		this.args = args;
	}
	
	public ItemStack getItem(){
		return item;
	}
	
	public ParseResult setDurability(){
		if(item == null || item.getItemMeta() == null) return ParseResult.NULL_ITEM;
		if(args.length != 1) return ParseResult.NO_ARGS;
		int durability = tryParse(args[0], -1);
		if(durability < 0 || durability > 100) return ParseResult.NO_PERCENT;
		short maxDurability = item.getType().getMaxDurability();
		item = Adapter.SetDurability(item,(int) (maxDurability - (maxDurability * (double)durability/100)));
		return ParseResult.SUCCESS;
	}
	
	public ParseResult setLore(){
		if(item == null || item.getItemMeta() == null) return ParseResult.NULL_ITEM;
		if(args.length < 1) return ParseResult.NO_ARGS;
		int lineNumber = tryParse(args[0]);
		if(lineNumber == 0) return ParseResult.NO_NUMBER;
		String loreLine = ChatColor.translateAlternateColorCodes('&', joinRemaining(1));
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<String>();
		
		while(lore.size() < lineNumber)
			lore.add("");
		lore.set(lineNumber-1, loreLine);	
		meta.setLore(lore);
		item.setItemMeta(meta);
		return ParseResult.SUCCESS;
	}
	
	public ParseResult setDisplayName(){
		if(item == null || item.getItemMeta() == null) return ParseResult.NULL_ITEM;
		String name = ChatColor.translateAlternateColorCodes('&', joinRemaining(0));
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return ParseResult.SUCCESS;
	}
    /*
	public ParseResult setLocalizedName(){
		if(item == null || item.getItemMeta() == null) return ParseResult.NULL_ITEM;
		String name = ChatColor.translateAlternateColorCodes('&', joinRemaining(0));
		ItemMeta meta = item.getItemMeta();
		meta.setLocalizedName(name);
		item.setItemMeta(meta);
		return ParseResult.SUCCESS;
	}*/
	
	private void clearEnchants(){
		ItemMeta meta = item.getItemMeta();
		meta.getEnchants().keySet().forEach(x -> meta.removeEnchant(x));
		item.setItemMeta(meta);
	}
	
	public ParseResult setItemFlags(){
		if(item == null || item.getItemMeta() == null) return ParseResult.NULL_ITEM;
		if(args.length < 1) return ParseResult.NO_ARGS;
		ItemMeta meta = item.getItemMeta();
		ItemFlag flag;
		while(args.length > 0){
			flag = getItemFlag(popFirstArg());
			if(flag == null) return ParseResult.INVALID_ITEMFLAG;
			if(meta.getItemFlags().contains(flag)) meta.removeItemFlags(flag);
			else meta.addItemFlags(flag);
				
			
		}
		item.setItemMeta(meta);
		return ParseResult.SUCCESS;
	}
	
	public ParseResult enchant(){
		if(item == null || item.getItemMeta() == null) return ParseResult.NULL_ITEM;
		if(args.length < 2) return ParseResult.NO_ARGS;
		if(args.length % 2 != 0) return ParseResult.MISSING_VALUE;
		clearEnchants();
		Enchantment currentEnch;
		int currentLevel;
		while(args.length > 0){
			currentEnch = getEnchantment(popFirstArg());
			if(currentEnch == null){
				return ParseResult.INVALID_ENCHANTMENT;
			}
			currentLevel = tryParse(popFirstArg());
			if(currentLevel == 0)
				return ParseResult.NO_NUMBER;
			
			addEnchantment(currentEnch, currentLevel);
		}
		return ParseResult.SUCCESS;
		
	}
	
	private ItemFlag getItemFlag(String arg){
		try{
			return ItemFlag.valueOf(arg.toUpperCase());
		}catch(Exception e){
			return null;
		}
	}
	
	private Enchantment getEnchantment(String arg){
		try{
		    Enchantment olderMethod = EnchantmentUtil.getByName(arg);
		    return olderMethod;
			//return EnchantmentWrapper.getByKey(NamespacedKey.minecraft(arg));
		}catch(Exception e){
			return null;
		}
	}
	
	private void addEnchantment(Enchantment ench, int level){
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(ench, level, true);
		item.setItemMeta(meta);
	}
	
	private static int tryParse(String arg){
		return tryParse(arg, 0);
	}
	
	private static int tryParse(String arg, int defaultVal){
		try{
			return Integer.parseInt(arg);
		} catch(NumberFormatException e){
			return defaultVal;
		}
	}
	
	private String joinRemaining(int start){
		String joined = "";
		for(int i = start; i < args.length; i++)
			joined += args[i] + (i+1 == args.length ? "" :" ");
		return joined;
	}
	
	private String popFirstArg(){
		String first = args[0];
		String newArgs[] = new String[args.length-1];
		for(int i = 0; i < args.length-1; i++){
			newArgs[i] = args[i+1];
		}
		args = newArgs;
		return first;
	}
	
}





