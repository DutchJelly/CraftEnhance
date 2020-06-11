package com.dutchjelly.craftenhance.crafthandling.util;


import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class ServerRecipeTranslator {

    private static final String KeyPrefix = "cehrecipe";

    private static List<String> UsedKeys = new ArrayList<>();

    public static ShapedRecipe translateShapedEnhancedRecipe(ItemStack[] content, ItemStack result, String key){
        Random r = new Random();
        String recipeKey = key.toLowerCase().replaceAll("[^a-z0-9 ]", "");
        recipeKey = recipeKey.trim();
//        while(UsedKeys.contains(recipeKey)) recipeKey += String.valueOf(r.nextInt(10));
        if(!UsedKeys.contains(recipeKey))
            UsedKeys.add(recipeKey);
        ShapedRecipe shaped = Adapter.GetShapedRecipe(
                CraftEnhance.getPlugin(CraftEnhance.class), KeyPrefix + recipeKey, result
        );
        shaped.shape(GetShape(content));
        MapIngredients(shaped, content);
        return shaped;
    }

    public static ShapedRecipe translateShapedEnhancedRecipe(WBRecipe recipe){
        return translateShapedEnhancedRecipe(recipe.getContent(), recipe.getResult(), recipe.getKey());
    }



    public static ShapelessRecipe translateShapelessEnhancedRecipe(ItemStack[] content, ItemStack result, String key){
        Bukkit.getLogger().log(Level.SEVERE, "shapeless recipe are not implemented yet");
        return null;
    }

    public static ShapelessRecipe translateShapelessEnhancedRecipe(WBRecipe recipe){
        return translateShapelessEnhancedRecipe(recipe.getContent(), recipe.getResult(), recipe.getKey());
    }


    public static ItemStack[] translateShapedRecipe(ShapedRecipe recipe){
        ItemStack[] content = new ItemStack[9];
        String[] shape = recipe.getShape();
        int columnIndex;
        for(int i = 0; i < shape.length; i++){
            columnIndex = 0;
            for(char c : shape[i].toCharArray()){
                content[(i*3) + columnIndex] = recipe.getIngredientMap().get(c);
                columnIndex++;
            }
        }
        return content;
    }

    public static ItemStack[] translateShapelessRecipe(ShapelessRecipe recipe){
        if(recipe == null || recipe.getIngredientList() == null) return null;
        return recipe.getIngredientList().toArray(new ItemStack[recipe.getIngredientList().size()]);
    }


    //Gets the shape of the recipe 'content'.
    private static String[] GetShape(ItemStack[] content){
        String recipeShape[] = {"","",""};
        for(int i = 0; i < 9; i++){
            if(content[i] != null)
                recipeShape[i/3] += (char)('A' + i);
            else
                recipeShape[i/3] += ' ';
        }
        return TrimShape(recipeShape);
    }

    //Trims the shape so that there are no redundant spaces or elements in shape.
    private static String[] TrimShape(String[] shape){
        List<String> TrimmedShape = new ArrayList<>();
        int maxLength = 0;
        int temp;
        for(int i = 0; i < shape.length; i++){
            temp = StringUtils.stripEnd(shape[i], " ").length();
            if(temp > maxLength)
                maxLength = temp;
        }
        for(int i = 0; i < shape.length; i++){
            shape[i] = shape[i].substring(0, maxLength);
            if(shape[i].trim().length() > 0) TrimmedShape.add(shape[i]);
        }
        return TrimmedShape.toArray(new String[0]);
    }

    private static void MapIngredients(ShapedRecipe recipe, ItemStack[] content){
        for(int i = 0; i < 9; i++){
            if(content[i] != null){
                //recipe.setIngredient((char) ('A' + i), content[i].getType());
                Adapter.SetIngredient(recipe, (char) ('A' + i), content[i]);
            }
        }
    }

}
