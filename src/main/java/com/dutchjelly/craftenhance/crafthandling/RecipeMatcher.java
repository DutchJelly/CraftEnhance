package com.dutchjelly.craftenhance.crafthandling;


import org.bukkit.inventory.ItemStack;

public class RecipeMatcher {



    public static boolean matchShapeless(ItemStack[] a, ItemStack[] b, Matcher m){
        if(a.length != b.length) return false;
        //TODO account for null values....
        boolean[] matched = new boolean[a.length];
        for(ItemStack item : a){
            for(int i = 0; i < b.length; i++){
                if(matched[i]) continue;

                if(m.match(item, b[i])) {
                    matched[i] = true;
                    break;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean matchShaped(ItemStack[] a, ItemStack[] b, Matcher m){
        return false;
    }
}
