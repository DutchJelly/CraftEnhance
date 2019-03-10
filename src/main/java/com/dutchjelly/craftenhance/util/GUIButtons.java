package com.dutchjelly.craftenhance.util;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIButtons {
	
	public static ItemStack save;
	public static ItemStack reset;
	public static ItemStack back;
	public static ItemStack filling;
	public static ItemStack next;
	public static ItemStack previous;
	public static ItemStack delete;
	
	public static void init(){
		//TODO make this configurable
		save = makeItem("&6Save changes", Material.EMERALD_ORE);
		reset = makeItem("&7Reset recipe", Material.REDSTONE);
		back = makeItem("&7Back to previous page", Material.GLOWSTONE_DUST);
//		filling = makeItem("", Material.GRAY_STAINED_GLASS_PANE);
		filling = makeGlassColor(makeItem("", Material.STAINED_GLASS_PANE), (short)7);
//		next = makeItem("&2Next", Material.LIGHT_BLUE_STAINED_GLASS_PANE);
		next = makeGlassColor(makeItem("&2Next", Material.STAINED_GLASS_PANE), (short)3);
//		previous = makeItem("&2Previous", Material.LIGHT_BLUE_STAINED_GLASS_PANE);
		previous = makeGlassColor(makeItem("&2Previous", Material.STAINED_GLASS_PANE), (short)3);
		delete = makeItem("&4&lDELETE", Material.REDSTONE_BLOCK);
	}
	
	private static ItemMeta setBasicMeta(ItemMeta meta){
		meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		return meta;
	}
	
	private static ItemStack makeItem(String name, Material type){
		ItemStack item = new ItemStack(type);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		item.setItemMeta(setBasicMeta(meta));
		return item;
	}
	
	//for versions below 1.13. 

	private static ItemStack makeGlassColor(ItemStack item, short color){
		ItemMeta meta = item.getItemMeta();
		ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
		glass.setItemMeta(meta);
		return glass;
	}

}
