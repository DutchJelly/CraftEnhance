package com.dutchjelly.craftenhance.crafthandling.util;

import org.bukkit.inventory.ItemStack;

public class ItemMatchers{


    public static boolean matchMeta(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.isSimilar(b);
    }

    public static boolean matchType(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.getType().equals(b.getType());
    }
}
