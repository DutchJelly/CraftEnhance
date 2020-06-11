package com.dutchjelly.craftenhance.crafthandling.util;

import com.dutchjelly.craftenhance.IMatcher;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.logging.Level;

public class WBRecipeComparer {


    public static boolean shapeMatches(ItemStack[] content, ItemStack[] r, IMatcher<ItemStack> matcher){
        //Find the first element of r and content.
        int i = -1, j = -1;
        while(++i < r.length && r[i] == null);
        while(++j < content.length && content[j] == null);

        //Look if one or both recipes are empty. Return true if both are empty.
        if(i == r.length || j == content.length) return i == r.length && j == content.length;

        int iVisited = 0, jVisited = 0;
        int iLine = 0, jLine = 0;

        //Now iterate over the first shape, assuming that the second shape is equal.
        for(; i < r.length && j < content.length; i++){
            if(r[i] != null && content[j] != null){
                iVisited++; jVisited++;

                //Check if the two elements are found in different lines.
                if(jLine != iLine) return false;
            }

            if(!matcher.match(r[i], content[j]))
                return false;

            //If one of the counters skips to the next line, the corresponding line counter is incremented. Also
            //The other counter is incremented and is checked to be null and in range of the array. If needed the
            //line offset of the other counter is also incremented.
            if((i+1) % 3 == 0 && (j+1) % 3 != 0){
                j++;
                if(j >= content.length || content[j] != null) return false;
                iLine++;
                if((j+1) % 3 == 0) jLine++;
            }

            else if((j+1) % 3 == 0 && (i+1) % 3 != 0){
                i++;
                if(i >= r.length || r[i] != null) return false;
                jLine++;
                if((i+1) % 3 == 0) iLine++;
            }
        }

        return iVisited == jVisited;
    }

    public static boolean ingredientsMatch(ItemStack[] content, ItemStack[] ingredients, IMatcher<ItemStack> matcher){
        //array with all values to false.
        boolean[] used = new boolean[ingredients.length];

        int totalInRecipeCount = 0;
        for(ItemStack inRecipe : content){
            if(inRecipe == null) continue;
            totalInRecipeCount++;
            //Look if inRecipe matches with an ingredient.
            for(int i = 0; i < used.length; i++) {
                if (used[i]) continue;
                if(ingredients[i] == null){
                    Bukkit.getLogger().log(Level.SEVERE, "Matching a null ingredient is not supported.");
                    return false;
                }

                if(matcher.match(ingredients[i], inRecipe))
                    used[i] = true;
            }
        }
        if(totalInRecipeCount != used.length) return false;

        return !Arrays.asList(used).contains(false);
    }

}
