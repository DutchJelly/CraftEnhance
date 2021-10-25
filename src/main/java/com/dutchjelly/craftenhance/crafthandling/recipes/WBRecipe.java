package com.dutchjelly.craftenhance.crafthandling.recipes;

import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import com.dutchjelly.craftenhance.crafthandling.util.WBRecipeComparer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class WBRecipe extends EnhancedRecipe {

    @Getter @Setter
    private boolean shapeless = false; //false by default

    @Getter
    private RecipeType type = RecipeType.WORKBENCH;

    public WBRecipe(String perm, ItemStack result, ItemStack[] content){
        super(perm, result, content);
    }

    private WBRecipe(Map<String, Object> args){
        super(args);
        if(args.containsKey("shapeless"))
            shapeless = (Boolean) args.get("shapeless");
    }

    public WBRecipe(){

    }


    public static WBRecipe deserialize(Map<String,Object> args){

        WBRecipe recipe = new WBRecipe(args);
        if(args.containsKey("shapeless"))
            recipe.shapeless = (Boolean) args.get("shapeless");

        return recipe;
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<String, Object>(){{
            putAll(WBRecipe.super.serialize());
            put("shapeless", shapeless);
        }};
    }




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
            boolean result = WBRecipeComparer.ingredientsMatch(getContent(), ingredients, ItemMatchers::matchType);
            if(result)
                Bukkit.getLogger().log(Level.INFO, "matching shapeless for recipe " + getResult() + ": " + r.getResult());

            return result;
        }


        if(r instanceof ShapedRecipe){
            ItemStack[] shapedContent = ServerRecipeTranslator.translateShapedRecipe((ShapedRecipe)r);
            if(shapeless){
                return WBRecipeComparer.ingredientsMatch(shapedContent, getContent(), ItemMatchers::matchType);
            }
            return WBRecipeComparer.shapeMatches(getContent(), shapedContent, ItemMatchers::matchType);
        }
        return false;
    }

    //Looks if r is always similar to this (so we know it doesn't have to be loaded in again)
    @Override
    public boolean isAlwaysSimilar(Recipe r){
        if(!ItemMatchers.matchItems(r.getResult(), getResult())) //different result means it needs to be loaded in
            return false;

        if(r instanceof ShapelessRecipe){ //shapeless to shaped or shapeless is always similar
            ItemStack[] ingredients = ServerRecipeTranslator.translateShapelessRecipe((ShapelessRecipe) r);
            return WBRecipeComparer.ingredientsMatch(getContent(), ingredients, ItemMatchers::matchTypeData);
        }

        if(r instanceof ShapedRecipe && !shapeless){ //shaped to shaped (not shapeless) is similar
            ItemStack[] shapedContent = ServerRecipeTranslator.translateShapedRecipe((ShapedRecipe)r);
            return WBRecipeComparer.shapeMatches(getContent(), shapedContent, ItemMatchers::matchTypeData);
        }
        return false;
    }

    @Override
    public boolean isSimilar(EnhancedRecipe r) {
        if(r == null) return false;
        if(!(r instanceof WBRecipe)) return false;

        WBRecipe wbr = (WBRecipe)r;
        if(wbr.isShapeless() || shapeless){
            return WBRecipeComparer.ingredientsMatch(getContent(), wbr.getContent(), ItemMatchers::matchType);
        }
        return WBRecipeComparer.shapeMatches(getContent(), wbr.getContent(), ItemMatchers::matchType);
    }

    @Override
    public boolean matches(ItemStack[] content) {
        if(isShapeless() && WBRecipeComparer.ingredientsMatch(content, getContent(), getMatchType().getMatcher())){
            return true;
        }

        if(!isShapeless() && WBRecipeComparer.shapeMatches(content, getContent(), getMatchType().getMatcher())){
            return true;
        }

        return false;
    }


}
