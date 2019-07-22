package com.dutchjelly.bukkitadapter;


import org.bukkit.Material;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
//import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;

import java.util.Arrays;
import java.util.List;

public class Adapter {
      //1.13 and up
//    public static ItemStack GetStainedGlassPane(AColor color) {
//        switch(color){
//            case RED: return new ItemStack(Material.RED_STAINED_GLASS_PANE);
//            case BLACK: return new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
//            case BLUE: return new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
//            case BROWN: return new ItemStack(Material.BROWN_STAINED_GLASS_PANE);
//            case CYAN: return new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
//            case GRAY: return new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
//            case GREEN: return new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
//            case LIGHT_BLUE: return new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
//            case LIGHT_GRAY: return new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
//            case LIME: return new ItemStack(Material.LIME_STAINED_GLASS_PANE);
//            case MAGENTA: return new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
//            case ORANGE: return new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
//            case PINK: return new ItemStack(Material.PINK_STAINED_GLASS_PANE);
//            case PURPLE: return new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
//            case WHITE: return new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
//            case YELLOW: return new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
//        }
//        return null;
//    }

    public static List<String> CompatibleVersions(){
        return Arrays.asList("1.12");
        //return Arrays.asList("1.13", "1.14", "1.15");
        //return Arrays.asList("1.9", "1.10", "1.11");
    }


    //1.12 and up
    public static ShapedRecipe GetShapedRecipe(JavaPlugin plugin, String key, ItemStack result) {
        return new ShapedRecipe(new NamespacedKey(plugin, key),result);
    }

    //1.12 and lower
    public static ItemStack GetStainedGlassPane(AColor color) {
        DyeColor dyeColor = DyeColor.valueOf(color.name());
        return new ItemStack(Material.STAINED_GLASS_PANE, 1, dyeColor.getDyeData());
    }

    //1.12 and lower
    public static ItemStack SetDurability(ItemStack item, int damage){
        short maxDurability = item.getType().getMaxDurability();
        item.setDurability((short)damage);
        return item;
    }

    //1.12 and lower
    public static void SetIngredient(ShapedRecipe recipe, char key, ItemStack ingredient){
        recipe.setIngredient(key, ingredient.getData());
    }
    //1.13 and up
//    public static void SetIngredient(ShapedRecipe recipe, char key, ItemStack ingredient){
//        recipe.setIngredient(key, ingredient.getType());
//    }

    //1.13 and up
//    public static ItemStack SetDurability(ItemStack item, int damage){
//        Damageable meta = (Damageable)item.getItemMeta();
//        meta.setDamage(damage);
//        item.setItemMeta((ItemMeta)meta);
//        return item;
//    }

    public static Material GetWorkBench(){
        //return Material.CRAFTING_TABLE;
        return Material.WORKBENCH;
    }

      //1.11 and lower
//    public static ShapedRecipe GetShapedRecipe(JavaPlugin plugin, String key, ItemStack result) {
//        return new ShapedRecipe(result);
//    }
}
