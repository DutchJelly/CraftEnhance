package com.dutchjelly.craftenhance.crafthandling.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemMatchers{


    public static boolean matchItems(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.equals(b);
    }

    private static boolean matchModelData(ItemStack a, ItemStack b){
        ItemMeta am = a.getItemMeta(), bm = b.getItemMeta();
        return am.hasCustomModelData() == bm.hasCustomModelData() && (!am.hasCustomModelData() || am.getCustomModelData() == bm.getCustomModelData());
    }

    public static boolean matchMeta(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;

        //Also use this method in case some ItemStack has an overwritten method.
        if(a.isSimilar(b)) return true;

        //We use toString() because somehow items can be stored differently in memory, which breaks #isSimilar
        return a.getType().equals(b.getType()) && a.getDurability() == b.getDurability() && a.hasItemMeta() == b.hasItemMeta() && (!a.hasItemMeta() || (
                a.getItemMeta().toString().equals(b.getItemMeta().toString())) && matchModelData(a, b)
        );
    }

    public static boolean matchType(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.getType().equals(b.getType());
    }


    public static boolean matchTypeData(ItemStack a, ItemStack b){

        if(a == null || b == null) return a == null && b == null;

        if(a.getData() == null && b.getData() == null)
            return matchType(a, b);
        return a.getData().equals(b.getData());
    }
}
