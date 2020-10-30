package com.dutchjelly.craftenhance.crafthandling.recipes;

import com.dutchjelly.craftenhance.ConfigError;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.IEnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import com.dutchjelly.craftenhance.crafthandling.util.WBRecipeComparer;
import com.dutchjelly.craftenhance.files.FileManager;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
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
import java.util.logging.Level;

public class WBRecipe implements IEnhancedRecipe {

    public WBRecipe(){

    }

    public WBRecipe(String perm, ItemStack result, ItemStack[] content){
        this.permissions = perm;
        this.result = result;
        this.content = content;
    }

    @Getter @Setter
    private int slot = -1;

    @Getter @Setter
    private int page = -1;

    @Getter @Setter
    private int id;

    @Getter @Setter
    private String key;

    @Getter @Setter
    private ItemStack result;

    @Getter @Setter
    private ItemStack[] content;

    @Getter @Setter
    private boolean shapeless = false; //false by default

    @Getter @Setter
    private boolean matchMeta = true; //true by default

    @Getter @Setter
    private String permissions;

    @Getter @Setter
    private boolean hidden;


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
            boolean result = WBRecipeComparer.ingredientsMatch(content, ingredients, ItemMatchers::matchType);
            if(result)
                Bukkit.getLogger().log(Level.INFO, "matching shapeless for recipe " + getResult() + ": " + r.getResult());

            return result;
        }


        if(r instanceof ShapedRecipe){
            ItemStack[] shapedContent = ServerRecipeTranslator.translateShapedRecipe((ShapedRecipe)r);
            if(shapeless){
                return WBRecipeComparer.ingredientsMatch(shapedContent, content, ItemMatchers::matchType);
            }
            return WBRecipeComparer.shapeMatches(content, shapedContent, ItemMatchers::matchType);
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
            return WBRecipeComparer.ingredientsMatch(content, ingredients, ItemMatchers::matchTypeData);
        }

        if(r instanceof ShapedRecipe && !shapeless){ //shaped to shaped (not shapeless) is similar
            ItemStack[] shapedContent = ServerRecipeTranslator.translateShapedRecipe((ShapedRecipe)r);
            return WBRecipeComparer.shapeMatches(content, shapedContent, ItemMatchers::matchTypeData);
        }
        return false;
    }

    @Override
    public boolean isSimilar(IEnhancedRecipe r) {
        if(r == null) return false;
        if(!(r instanceof WBRecipe)) return false;

        WBRecipe wbr = (WBRecipe)r;
        if(wbr.isShapeless() || shapeless){
            return WBRecipeComparer.ingredientsMatch(content, wbr.getContent(), ItemMatchers::matchType);
        }
        return WBRecipeComparer.shapeMatches(content, wbr.getContent(), ItemMatchers::matchType);
    }


    public static WBRecipe deserialize(Map<String,Object> args){
        if(CraftEnhance.self() == null) return null;

        FileManager fm = CraftEnhance.self().getFm();

        List<String> recipeKeys;
        WBRecipe recipe = new WBRecipe();
        recipe.result = fm.getItem((String)args.get("result"));
        recipe.permissions = (String)args.get("permission");
        if(args.containsKey("shapeless"))
            recipe.shapeless = (Boolean) args.get("shapeless");
        if(args.containsKey("matchmeta"))
            recipe.matchMeta = (Boolean) args.get("matchmeta");
        if(args.containsKey("hidden"))
            recipe.hidden = (Boolean) args.get("hidden");
        if(args.containsKey("slot"))
            recipe.slot = (int)args.get("slot");
        if(args.containsKey("page"))
            recipe.page = (int)args.get("page");
        recipe.setContent(new ItemStack[9]);
        recipeKeys = (List<String>)args.get("recipe");
        for(int i = 0; i < recipe.content.length; i++){
            recipe.content[i] = fm.getItem(recipeKeys.get(i));
        }

        return recipe;
    }

    @Override
    public Map<String, Object> serialize() {
        FileManager fm = CraftEnhance.getPlugin(CraftEnhance.class).getFm();

        Map<String, Object> serialized = new HashMap<>();
        serialized.put("permission", permissions);
        serialized.put("shapeless", shapeless);
        serialized.put("matchmeta", matchMeta);
        serialized.put("hidden", hidden);
        serialized.put("slot", slot);
        serialized.put("page", page);
        serialized.put("result", fm.getItemKey(result));

        String recipeKeys[] = new String[content.length];
        for(int i = 0; i < content.length; i++){
            recipeKeys[i] = fm.getItemKey(content[i]);
        }
        serialized.put("recipe", recipeKeys);
        return serialized;
    }

    @Override
    public String validate(){
        if(result == null)
            return "recipe cannot have null result";

        if(content.length == 0 || !Arrays.stream(content).anyMatch(x -> x != null))
            return "recipe content cannot be empty";

        return null;
    }

    @Override
    public String toString(){
        String s = "";
        s += "key = " + key + "\n";
        s += "result = " + result == null ? "null" : result.toString() + "\n";
        return s;
    }




}
