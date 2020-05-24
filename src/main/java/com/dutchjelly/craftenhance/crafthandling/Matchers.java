package com.dutchjelly.craftenhance.crafthandling;

import org.bukkit.inventory.ItemStack;

public class Matchers {

    public static boolean matchMeta(ItemStack a, ItemStack b){
        if(a == null && b == null) return true;
        if(a == null || b == null) return false;
        return a.isSimilar(b);
    }

    public static boolean matchType(ItemStack a, ItemStack b){
        if(a == null && b == null) return true;
        if(a == null || b == null) return false;
        return a.getType().equals(b.getType());
    }

}
