package com.dutchjelly.craftenhance.crafthandling.util;

import org.bukkit.inventory.ItemStack;

public class ItemMatchers{


    public static boolean matchItems(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.equals(b);
    }

    public static boolean matchMeta(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        //TODO find a way to not have to use toString() to compare item meta...

        //Also use this method in case some ItemStack has an overwritten method.
        if(a.isSimilar(b)) return true;

        return a.getType().equals(b.getType()) && a.getDurability() == b.getDurability() && a.hasItemMeta() == b.hasItemMeta() && (!a.hasItemMeta() || a.getItemMeta().toString().equals(b.getItemMeta().toString()));
    }

    public static boolean matchType(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.getType().equals(b.getType());
    }


    public static boolean matchTypeData(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.getData().equals(b.getData());
    }
}
