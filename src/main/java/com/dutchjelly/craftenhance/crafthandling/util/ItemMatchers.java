package com.dutchjelly.craftenhance.crafthandling.util;

import com.dutchjelly.bukkitadapter.Adapter;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ItemMatchers{

    public enum MatchType {

        MATCH_TYPE(constructIMatcher(ItemMatchers::matchType), "match type"),
        MATCH_META(constructIMatcher(ItemMatchers::matchMeta), "match meta"),
        MATCH_NAME(constructIMatcher(ItemMatchers::matchName), "match name"),
        MATCH_MODELDATA_AND_TYPE(constructIMatcher(ItemMatchers::matchType, ItemMatchers::matchModelData), "match modeldata and type");
//        MATCH_ITEMSADDER()
//        MATCH_NAME_AND_TYPE(constructIMatcher(ItemMatchers::matchName, ItemMatchers::matchType), "match name and type");

        @Getter
        private IMatcher matcher;

        @Getter
        private String description;


        MatchType(IMatcher matcher, String description) {
            this.matcher = matcher;
            this.description = description;
        }
    }

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

        if(backwardsCompatibleMatching) {
            return a.getType().equals(b.getType()) && a.getDurability() == b.getDurability() && a.hasItemMeta() == b.hasItemMeta() && (!a.hasItemMeta() || (
                    a.getItemMeta().toString().equals(b.getItemMeta().toString()))
                    && (canUseModeldata && matchModelData(a, b) || !canUseModeldata)
            );
        }

        return a.isSimilar(b) && (canUseModeldata && matchModelData(a,b) || !canUseModeldata);
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

    public static boolean matchName(ItemStack a, ItemStack b){
        if(a.hasItemMeta() && b.hasItemMeta()) {
            return a.getItemMeta().getDisplayName().equals(b.getItemMeta().getDisplayName());
        }
        //neither has item meta, and type has to match
        return a.hasItemMeta() == b.hasItemMeta() && a.getType() == b.getType();
    }

//    public static boolean matchItemsadderItems(ItemStack a, ItemStack b) {
//        CustomStack stack = CustomStack.byItemStack(myItemStack);
//        CustomStack
//    }
}
