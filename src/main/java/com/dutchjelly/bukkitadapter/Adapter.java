package com.dutchjelly.bukkitadapter;


import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.messaging.Debug;
import org.bukkit.Keyed;
import org.bukkit.Material;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.DyeColor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Adapter {


    public static List<String> CompatibleVersions(){
        return Arrays.asList("1.9", "1.10", "1.11", "1.12", "1.13", "1.14", "1.15", "1.16", "1.17", "1.18");
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

    private static Optional<Boolean> canUseModeldata = Optional.empty();
    public static boolean canUseModeldata() {
        if(canUseModeldata.isPresent()) {
            return canUseModeldata.get();
        }
        try {
            ItemMeta.class.getMethod("getCustomModelData");
            canUseModeldata = Optional.of(true);
            return true;
        } catch (NoSuchMethodException e) {
            canUseModeldata = Optional.of(false);
            return false;
        }
    }

    private static Object getNameSpacedKey(JavaPlugin plugin, String key) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
//        return new NamespacedKey(plugin, key);
        return Class.forName("org.bukkit.NamespacedKey").getConstructor(org.bukkit.plugin.Plugin.class, String.class).newInstance(plugin, key);
    }

    public static ShapedRecipe GetShapedRecipe(JavaPlugin plugin, String key, ItemStack result){
        try{
            return ShapedRecipe.class.getConstructor(Class.forName("org.bukkit.NamespacedKey"), ItemStack.class).newInstance(getNameSpacedKey(plugin, key), result);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            Debug.Send("Couldn't use namespaced key: " + e.getMessage() + "\n" + e.getStackTrace());
        }
        return new ShapedRecipe(result);
    }

    public static ShapelessRecipe GetShapelessRecipe(JavaPlugin plugin, String key, ItemStack result){
        try {
            return ShapelessRecipe.class.getConstructor(Class.forName("org.bukkit.NamespacedKey"), ItemStack.class).newInstance(getNameSpacedKey(plugin, key), result);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            Debug.Send("Couldn't use namespaced key: " + e.getMessage() + "\n" + e.getStackTrace());
        }
        return new ShapelessRecipe(result);
    }

    private static <T> boolean callSingleParamMethod(String methodName, T param, Class<T> paramType, Object instance, Class<?> instanceType) {
        try {
            Method m = instanceType.getMethod(methodName, paramType);
            m.invoke(instance, param);

            return true;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }

    public static FurnaceRecipe GetFurnaceRecipe(JavaPlugin plugin, String key, ItemStack result, Material source, int duration, float exp){
        //public FurnaceRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull Material source, float experience, int cookingTime) {
        try{
            return FurnaceRecipe.class.getConstructor(Class.forName("org.bukkit.NamespacedKey"), ItemStack.class, Material.class, float.class, int.class)
                    .newInstance(getNameSpacedKey(plugin, key), result, source, exp, duration);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            Debug.Send("Couldn't use namespaced key: " + e.getMessage() + "\n" + e.getStackTrace());
            e.printStackTrace();
        }
        FurnaceRecipe recipe = new FurnaceRecipe(result, source);

        if(!callSingleParamMethod("setCookingTime", duration, Integer.class, recipe, FurnaceRecipe.class))
            Debug.Send("Custom cooking time is not supported.");
        recipe.setExperience(exp);
        return recipe;
    }



    public static ItemStack SetDurability(ItemStack item, int damage){
        item.setDurability((short)damage);
        return item;
    }

    public static void SetIngredient(ShapedRecipe recipe, char key, ItemStack ingredient){
        if(!CraftEnhance.self().getConfig().getBoolean("learn-recipes")){
            MaterialData md = ingredient.getData();
            if(md == null || !md.getItemType().equals(ingredient.getType()) || md.getItemType().equals(Material.AIR)){
                recipe.setIngredient(key, ingredient.getType());
            }else{
                recipe.setIngredient(key, md);
            }
            return;
        }
        try{
            recipe.getClass().getMethod("setIngredient", char.class, Class.forName("org.bukkit.inventory.RecipeChoice.ExactChoice")).invoke(recipe,
                    key, Class.forName("org.bukkit.inventory.RecipeChoice.ExactChoice").getConstructor(ItemStack.class).newInstance(ingredient)
            );
        }catch(Exception e){
            recipe.setIngredient(key, ingredient.getType());
        }
    }

    public static void AddIngredient(ShapelessRecipe recipe, ItemStack ingredient){
        if(!CraftEnhance.self().getConfig().getBoolean("learn-recipes")){
            MaterialData md = ingredient.getData();
            if(md == null || !md.getItemType().equals(ingredient.getType()) || md.getItemType().equals(Material.AIR)){
                recipe.addIngredient(ingredient.getType());
            }else{
                recipe.addIngredient(md);
            }
            return;
        }
        try{
            recipe.getClass().getMethod("addIngredient", Class.forName("org.bukkit.inventory.RecipeChoice.ExactChoice")).invoke(recipe,
                    Class.forName("org.bukkit.inventory.RecipeChoice.ExactChoice").getConstructor(ItemStack.class).newInstance(ingredient)
            );
        }catch(Exception e){
            recipe.addIngredient(ingredient.getType());
        }
    }

    public static void DiscoverRecipes(Player player, List<Recipe> recipes){
        try{
            for (Recipe recipe : recipes) {
                if(recipe instanceof Keyed) {
                    Keyed keyed = (Keyed)recipe;
                    player.discoverRecipe(keyed.getKey());
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

    public static boolean ContainsSubKey(Recipe r, String key) {
        String keyString = GetRecipeIdentifier(r);
        return keyString == null ? key == null : keyString.contains(key);
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
