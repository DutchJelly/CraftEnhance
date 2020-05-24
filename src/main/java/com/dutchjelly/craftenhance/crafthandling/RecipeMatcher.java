package com.dutchjelly.craftenhance.crafthandling;


import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class RecipeMatcher {

    private static boolean matchShapeless(ItemStack[] a, ItemStack[] b, Matcher m){
        return false;
    }

    private static boolean matchShaped(ItemStack[] a, ItemStack[] b, Matcher m){
        return false;
    }

    public static boolean match(CustomWBRecipe wbRecipe, ShapedRecipe r, Matcher m){
        if(r == null || wbRecipe == null) return false;
        
        return false;
    }

    public static boolean match(CustomWBRecipe wbRecipe, ShapelessRecipe r, Matcher m){
        return false;
    }
}
