package com.dutchjelly.craftenhance.crafthandling.util;

import com.dutchjelly.craftenhance.crafthandling.recipes.MatchType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemMatchers{

    public static IMatcher<ItemStack> fromMatchType(MatchType type) {
        switch (type) {
            case MATERIAL: return ItemMatchers::matchType;
            case META: return ItemMatchers::matchMeta;
            case NAME: return ItemMatchers::matchNameAndType;
        }
        return ItemMatchers::matchMeta;
    }


    public static boolean matchItems(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.equals(b);
    }

    private static boolean matchModelData(ItemStack a, ItemStack b){
        if(!a.hasItemMeta() || !b.hasItemMeta())
            return a.hasItemMeta() == b.hasItemMeta();
        ItemMeta am = a.getItemMeta(), bm = b.getItemMeta();
        if(!am.hasCustomModelData() || !bm.hasCustomModelData())
            return am.hasCustomModelData() == bm.hasCustomModelData();

        return am.getCustomModelData() == bm.getCustomModelData();
    }

    public static boolean matchMeta(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        //TODO find a way to not have to use toString() to compare item meta...

        boolean matchingModelData = matchModelData(a, b);

        //Also use this method in case some ItemStack has an overwritten method.
        if(matchingModelData && a.isSimilar(b)) return true;

        return matchingModelData && a.getType().equals(b.getType()) && a.getDurability() == b.getDurability() && a.hasItemMeta() == b.hasItemMeta() && (!a.hasItemMeta() || a.getItemMeta().toString().equals(b.getItemMeta().toString()));
    }

    public static boolean matchType(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.getType().equals(b.getType());
    }

    public static boolean matchNameAndType(ItemStack a, ItemStack b){
        if(!matchType(a,b)) return false;

        if(!a.hasItemMeta() || !b.hasItemMeta())
            return a.hasItemMeta() == b.hasItemMeta();
        return a.getItemMeta().getDisplayName().equals(b.getItemMeta().getDisplayName());
    }


    public static boolean matchTypeData(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.getData().equals(b.getData());
    }
}
