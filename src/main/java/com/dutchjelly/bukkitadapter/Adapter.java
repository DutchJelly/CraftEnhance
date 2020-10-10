package com.dutchjelly.bukkitadapter;


import com.dutchjelly.craftenhance.messaging.Debug;
import lombok.SneakyThrows;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Material;

import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.DyeColor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class Adapter {


    public static List<String> CompatibleVersions(){
        return Arrays.asList("1.9", "1.10", "1.11", "1.12", "1.13", "1.14", "1.15", "1.16");
    }


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

    @SuppressWarnings("deprecation")
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

    @SneakyThrows
    private static Object getNameSpacedKey(JavaPlugin plugin, String key){
        return Class.forName("org.bukkit.NamespacedKey").getConstructor(JavaPlugin.class, String.class).newInstance(plugin, key);
    }

    public static ShapedRecipe GetShapedRecipe(JavaPlugin plugin, String key, ItemStack result){
        try{
            return ShapedRecipe.class.getConstructor(Class.forName("org.bukkit.NamespacedKey"), ItemStack.class).newInstance(getNameSpacedKey(plugin, key), result);
        } catch(Exception e){ }
        return new ShapedRecipe(result);
    }

    public static ShapelessRecipe GetShapelessRecipe(JavaPlugin plugin, String key, ItemStack result){
        try{
            return ShapelessRecipe.class.getConstructor(Class.forName("org.bukkit.NamespacedKey"), ItemStack.class).newInstance(getNameSpacedKey(plugin, key), result);
        } catch(Exception e){ }
        return new ShapelessRecipe(result);
    }

    public static ItemStack SetDurability(ItemStack item, int damage){
        //TODO This is deprecated, do it with DamageableMeta.
        item.setDurability((short)damage);
        return item;
    }


    public static void SetIngredient(ShapedRecipe recipe, char key, ItemStack ingredient){
        try{
            recipe.getClass().getMethod("setIngredient", char.class, Class.forName("org.bukkit.inventory.RecipeChoice.ExactChoice")).invoke(recipe,
                    key, Class.forName("org.bukkit.inventory.RecipeChoice.ExactChoice").getConstructor(ItemStack.class).newInstance(ingredient)
            );
        }catch(Exception e){
            recipe.setIngredient(key, ingredient.getData());
        }
    }

    public static void AddIngredient(ShapelessRecipe recipe, ItemStack ingredient){
        try{
            recipe.getClass().getMethod("addIngredient", Class.forName("org.bukkit.inventory.RecipeChoice.ExactChoice")).invoke(recipe,
                    Class.forName("org.bukkit.inventory.RecipeChoice.ExactChoice").getConstructor(ItemStack.class).newInstance(ingredient)
            );
        }catch(Exception e){
            recipe.addIngredient(ingredient.getData());
        }
    }

    public static void DiscoverRecipes(Player player, List<Recipe> recipes){
        try{
            for (Recipe recipe : recipes) {
                if(recipe instanceof ShapedRecipe){
                    ShapedRecipe shaped = (ShapedRecipe) recipe;
                    player.discoverRecipe(shaped.getKey());
                }else if(recipe instanceof ShapelessRecipe){
                    ShapelessRecipe shapeless = (ShapelessRecipe) recipe;
                    player.discoverRecipe(shapeless.getKey());
                }
            }
        }catch(Exception e){ }
    }

    public static void SetOwningPlayer(SkullMeta meta, OfflinePlayer player){
        try{
            meta.setOwningPlayer(player);
        }catch(Exception e){
            meta.setOwner(player.getName());
        }
    }

    public static Recipe FilterRecipes(List<Recipe> recipes, String name){
        for(Recipe r : recipes){
            String id = GetRecipeIdentifier(r);
            if(id == null) continue;
            if(id.equalsIgnoreCase(name))
                return r;
        }

        return recipes.stream().filter(x -> x != null).filter(x -> x.getResult().getType().name().equalsIgnoreCase(name)).findFirst().orElse(null);

    }


    public static String GetRecipeIdentifier(Recipe r){
        try{
            //reflection is so damn powerful!! You can even invoke methods from derived classes.
            Object obj = r.getClass().getMethod("getKey").invoke(r);
            if(obj != null) return obj.toString();
        }catch(Exception e){
        }

        return r.getResult().getType().name();
    }

}
