package com.dutchjelly.bukkitadapter;


import org.bukkit.Material;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.DyeColor;

import java.util.Arrays;
import java.util.List;

public class Adapter {


    /**
     * generic section
     */


    public static Material getMaterial(String name){
        try{
            return Material.valueOf(name);
        }catch(Exception e){
            if(name.equals("WORKBENCH"))
                return Material.valueOf("CRAFTING_TABLE");
            try{
                return Material.matchMaterial("LEGACY_" + name);
            }catch(Exception e2) {}
        }
        return null;
    }

    public static ItemStack getColoredItem(String name, DyeColor color){
        try{
            return new ItemStack(Material.valueOf(color.name() + "_" + name));
        }catch(Exception e){
            try{
                return new ItemStack(Material.valueOf(name), 1, (short)color.getWoolData());
            }catch(Exception e2){ }
        }
        return null;
    }

    /**
     * section for 1.9-1.11
     * TODO: add AddIngredient and GetShapelessRecipe to this section
     */

//    public static List<String> CompatibleVersions(){
//        return Arrays.asList("1.9", "1.10", "1.11");
//    }
//
//    public static ShapedRecipe GetShapedRecipe(JavaPlugin plugin, String key, ItemStack result) {
//           if(result == null) return null;
//        return new ShapedRecipe(result);
//    }
//
//    public static ShapelessRecipe GetShapelessRecipe(JavaPlugin plugin, String key, ItemStack result){
//           if(result == null) return null;
//        return new ShapelessRecipe(result);
//    }
//
//    public static ItemStack SetDurability(ItemStack item, int damage){
//        short maxDurability = item.getType().getMaxDurability();
//        item.setDurability((short)damage);
//        return item;
//    }
//
//    public static void SetIngredient(ShapedRecipe recipe, char key, ItemStack ingredient){
//        recipe.setIngredient(key, ingredient.getData());
//    }
//    public static void AddIngredient(ShapelessRecipe recipe, ItemStack ingredient){
//        recipe.addIngredient(ingredient.getData());
//    }



    /**
     * section for 1.12
     */

//    public static List<String> CompatibleVersions(){
//        return Arrays.asList("1.12");
//    }
//
//    public static ShapedRecipe GetShapedRecipe(JavaPlugin plugin, String key, ItemStack result) {
//        if(result == null) return null;
//        return new ShapedRecipe(new NamespacedKey(plugin, key),result);
//    }
//
//    public static ShapelessRecipe GetShapelessRecipe(JavaPlugin plugin, String key, ItemStack result){
//        if(result == null) return null;
//        return new ShapelessRecipe(new NamespacedKey(plugin, key), result);
//    }
//
//    //1.12 and lower
//    public static void SetIngredient(ShapedRecipe recipe, char key, ItemStack ingredient){
//        recipe.setIngredient(key, ingredient.getData());
//    }
//
//    public static void AddIngredient(ShapelessRecipe recipe, ItemStack ingredient){
//        recipe.addIngredient(ingredient.getData());
//    }
//
//    public static ItemStack SetDurability(ItemStack item, int damage){
//        short maxDurability = item.getType().getMaxDurability();
//        item.setDurability((short)damage);
//        return item;
//    }

    /**
     * section for 1.13+
     * TODO: add AddIngredient and GetShapelessRecipe to this section
     */

    public static List<String> CompatibleVersions(){
        return Arrays.asList("1.13", "1.14", "1.15", "1.16");
    }

    public static ShapedRecipe GetShapedRecipe(JavaPlugin plugin, String key, ItemStack result) {
        if(result == null) return null;
        return new ShapedRecipe(new NamespacedKey(plugin, key),result);
    }

    public static ShapelessRecipe GetShapelessRecipe(JavaPlugin plugin, String key, ItemStack result){
         if(result == null) return null;
        return new ShapelessRecipe(new NamespacedKey(plugin, key), result);
    }

    public static ItemStack SetDurability(ItemStack item, int damage){
        Damageable meta = (Damageable)item.getItemMeta();
        meta.setDamage(damage);
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    public static void AddIngredient(ShapelessRecipe recipe, ItemStack ingredient){
        recipe.addIngredient(ingredient.getType());
    }

    public static void SetIngredient(ShapedRecipe recipe, char key, ItemStack ingredient){
        recipe.setIngredient(key, ingredient.getType());
    }


}
