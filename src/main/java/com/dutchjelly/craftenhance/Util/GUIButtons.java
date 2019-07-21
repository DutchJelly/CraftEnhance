package com.dutchjelly.craftenhance.Util;


import com.dutchjelly.bukkitadapter.AColor;
import com.dutchjelly.bukkitadapter.Adapter;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
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
		filling = makeItem("", Adapter.GetStainedGlassPane(AColor.GRAY));
		next = makeItem("&2Next", Adapter.GetStainedGlassPane(AColor.LIGHT_BLUE));
		previous = makeItem("&2Previous", Adapter.GetStainedGlassPane(AColor.LIGHT_BLUE));
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
		return makeItem(name, item);
	}

	private static ItemStack makeItem(String name, ItemStack item){
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		item.setItemMeta(setBasicMeta(meta));
		return item;
	}

}
