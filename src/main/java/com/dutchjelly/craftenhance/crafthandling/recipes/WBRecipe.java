package com.dutchjelly.craftenhance.crafthandling.recipes;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.IEnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import com.dutchjelly.craftenhance.crafthandling.util.WBRecipeComparer;
import com.dutchjelly.craftenhance.files.FileManager;
import com.dutchjelly.craftenhance.model.CraftRecipe;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SerializableAs("Recipe")
public class WBRecipe implements IEnhancedRecipe, ConfigurationSerializable {


    @Getter @Setter
    private int id;

    @Getter @Setter
    private String key;

    @Getter @Setter
    private ItemStack result;

    @Getter @Setter
    private ItemStack[] content;

    @Getter @Setter
    private boolean shapeless;

    @Getter @Setter
    private boolean matchMeta;

    @Getter @Setter
    private String permissions;


    @Override
    public Recipe getServerRecipe() {
        if(shapeless)
            return ServerRecipeTranslator.translateShapelessEnhancedRecipe(this);
        return ServerRecipeTranslator.translateShapedEnhancedRecipe(this);
    }


    //The recipe is similar to a server recipe if theyre both shaped and their shapes match, if at least one is shaped and the ingredients match
    //Note that similar doesn't mean that the recipes are always equal. Shaped is always similar to shapeless, but not the other way around.
    @Override
    public boolean isSimilar(Recipe r) {
        if(r instanceof ShapelessRecipe){
            ItemStack[] ingredients = ServerRecipeTranslator.translateShapelessRecipe((ShapelessRecipe) r);
            return WBRecipeComparer.ingredientsMatch(content, ingredients, ItemMatchers::matchType);
        }


        if(r instanceof ShapedRecipe){
            ItemStack[] shapedContent = ServerRecipeTranslator.translateShapedRecipe((ShapedRecipe)r);
            if(shapeless){
                ItemStack[] trimmedIngredients = Arrays.asList(content).stream().filter(x -> x != null).toArray(ItemStack[]::new);
                return WBRecipeComparer.ingredientsMatch(shapedContent, trimmedIngredients, ItemMatchers::matchType);
            }
            return WBRecipeComparer.shapeMatches(content, shapedContent, ItemMatchers::matchType);
        }
        return false;
    }

    //Looks if r is always similar to this (so we know it doesn't have to be loaded in again)
    @Override
    public boolean isAlwaysSimilar(Recipe r){
        if(r instanceof ShapelessRecipe){ //shapeless to shaped is always similar
            ItemStack[] ingredients = ServerRecipeTranslator.translateShapelessRecipe((ShapelessRecipe) r);
            return WBRecipeComparer.ingredientsMatch(content, ingredients, ItemMatchers::matchType);
        }


        if(r instanceof ShapedRecipe && !shapeless){
            ItemStack[] shapedContent = ServerRecipeTranslator.translateShapedRecipe((ShapedRecipe)r);
            return WBRecipeComparer.shapeMatches(content, shapedContent, ItemMatchers::matchType);
        }
        return false;
    }

    @Override
    public boolean matches(Recipe r){
        return false;
    }


    public static WBRecipe deserialize(Map<String,Object> args){
        if(CraftEnhance.self() == null) return null;

        FileManager fm = CraftEnhance.self().getFileManager();

        List<String> recipeKeys;
        WBRecipe recipe = new WBRecipe();
        fm = CraftEnhance.getPlugin(CraftEnhance.class).getFileManager();
        recipe.result = fm.getItem((String)args.get("result"));
        recipe.permissions = (String)args.get("permission");
        recipe.shapeless = (Boolean) args.get("shapeless");
        recipe.setContent(new ItemStack[9]);
        recipeKeys = (List<String>)args.get("recipe");
        for(int i = 0; i < recipe.content.length; i++){
            recipe.content[i] = fm.getItem(recipeKeys.get(i));
        }
        return recipe;
    }

    @Override
    public Map<String, Object> serialize() {
        FileManager fm = CraftEnhance.getPlugin(CraftEnhance.class).getFileManager();

        Map<String, Object> serialized = new HashMap<String, Object>();
        serialized.put("permission", permissions);
        serialized.put("shapeless", shapeless);
        serialized.put("result", fm.getItemKey(result));
        String recipeKeys[] = new String[content.length];
        for(int i = 0; i < content.length; i++){
            recipeKeys[i] = fm.getItemKey(content[i]);
        }
        serialized.put("recipe", recipeKeys);
        return serialized;
    }





}
