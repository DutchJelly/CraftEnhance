package com.dutchjelly.craftenhance.crafthandling.util;

import com.dutchjelly.bukkitadapter.Adapter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ItemMatchers{

    private static boolean backwardsCompatibleMatching = false;

    public static void init(boolean backwardsCompatibleMatching) {
        ItemMatchers.backwardsCompatibleMatching = backwardsCompatibleMatching;
    }

    public static boolean matchItems(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.equals(b);
    }

    public static boolean matchModelData(ItemStack a, ItemStack b){
        ItemMeta am = a.getItemMeta(), bm = b.getItemMeta();
        if(am == null) return bm == null || !bm.hasCustomModelData();
        if(bm == null) return am == null || !am.hasCustomModelData();
        return am.hasCustomModelData() == bm.hasCustomModelData() && (!am.hasCustomModelData() || am.getCustomModelData() == bm.getCustomModelData());
    }

    public static boolean matchMeta(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        boolean canUseModeldata = Adapter.canUseModeldata();
        if(a.isSimilar(b) && (canUseModeldata && matchModelData(a,b) || !canUseModeldata)) return true;

        if(!backwardsCompatibleMatching) return false;

        //We use toString() because somehow items can be stored differently in memory, which breaks #isSimilar
        return a.getType().equals(b.getType()) && a.getDurability() == b.getDurability() && a.hasItemMeta() == b.hasItemMeta() && (!a.hasItemMeta() || (
                a.getItemMeta().toString().equals(b.getItemMeta().toString()))
                && (canUseModeldata && matchModelData(a, b) || !canUseModeldata)
        );
    }

    public static boolean matchType(ItemStack a, ItemStack b){
        if(a == null || b == null) return a == null && b == null;
        return a.getType().equals(b.getType());
    }

    public static <T> IMatcher<T> constructIMatcher(IMatcher<T>... matchers) {
        return (a,b) -> Arrays.stream(matchers).allMatch(x -> x.match(a,b));
    }


    public static boolean matchTypeData(ItemStack a, ItemStack b){

        if(a == null || b == null) return a == null && b == null;

        if(a.getData() == null && b.getData() == null)
            return matchType(a, b);
        return a.getData().equals(b.getData());
    }
}
