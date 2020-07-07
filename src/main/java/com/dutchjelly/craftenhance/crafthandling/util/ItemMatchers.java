package com.dutchjelly.craftenhance.crafthandling.util;

import com.dutchjelly.craftenhance.messaging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class ItemMatchers{


    public static boolean matchItems(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.equals(b);
    }

    public static boolean matchMeta(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        //TODO find a way to not have to use toString() to compare item meta...
        return a.getType().equals(b.getType()) && a.getDurability() == b.getDurability() && a.hasItemMeta() == b.hasItemMeta() && (!a.hasItemMeta() || a.getItemMeta().toString().equals(b.getItemMeta().toString()));
    }

    public static boolean matchType(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.getType().equals(b.getType());
    }

    //Looks if amount of a is <= amount of b in addition to checking type.
    public static boolean matchTypeAndAmount(ItemStack a, ItemStack b){
        return matchType(a,b) && (a == null || a.getAmount() <= b.getAmount());
    }

    //Looks if amount of a is <= amount of b in addition to checking meta.
    public static boolean matchMetaAndAmount(ItemStack a, ItemStack b){
        return matchMeta(a,b) && (a == null || a.getAmount() <= b.getAmount());
    }

    public static boolean matchTypeData(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.getData().equals(b.getData());
    }
}
