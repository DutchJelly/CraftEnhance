package com.dutchjelly.craftenhance.Util;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {

    public static ItemStack AddGlow(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack AddLore(ItemStack item, String text){
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if(lore == null) lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&',text));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack AddProperty(ItemStack item, String property, String value){
        return AddLore(item, "&f" + property + ": &e" + value);
    }

    public static ItemStack SetDisplayName(ItemStack item, String name){
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(meta);
        return item;
    }
}
