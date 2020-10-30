package com.dutchjelly.craftenhance.crafthandling.util;

import com.dutchjelly.craftenhance.IMatcher;
import com.dutchjelly.craftenhance.messaging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class WBRecipeComparer {

    private static ItemStack[] mirror(ItemStack[] content, int size){
        if(content == null) return null;
        if(content.length == 0) return content;
        ItemStack[] mirrored = new ItemStack[content.length];


        for(int i = 0; i < size; i++){

            //Walk through right and left elements of this row and swab them.
            for(int j = 0; j < size/2; j++){
                mirrored[i*size+j] = content[i*size+(size-j-1)];
                mirrored[i*size+(size-j-1)] = content[i*size+j];
            }

            //Copy middle item to mirrored.
            if(size%2 != 0)
                mirrored[i*size+(size/2)] = content[i*size+(size/2)];
        }
        return mirrored;
    }

    //This compares shapes and doesn't take mirrored recipes into account.
    //public for testing purposes. Not very professional I know, but it gets the job done.
    public static boolean shapeIterationMatches(ItemStack[] content, ItemStack[] r, IMatcher<ItemStack> matcher, int rowSize){
        //Find the first element of r and content.
        int i = -1, j = -1;
        while(++i < r.length && r[i] == null);
        while(++j < content.length && content[j] == null);

        //Look if one or both recipes are empty. Return true if both are empty.
        if(i == r.length || j == content.length) return i == r.length && j == content.length;

        if(!matcher.match(r[i],content[j])){
            return false;
        }

        //Offsets relative to the first item of the recipe.
        int iOffx, iOffy, jOffx, jOffy;
        for(;;) {
            iOffx = iOffy = 0;
            jOffx = jOffy = 0;
            while (++i < r.length) {
                iOffx++;
                if (i % rowSize == 0) iOffy++;

                if(r[i] != null) break;

            }
//            if (i % rowSize == 0) iOffy++;

            while (++j < content.length) {
                jOffx++;
                if (j % rowSize == 0) jOffy++;

                if(content[j] != null) break;
            }
//            if (j % rowSize == 0) jOffy++;

//            Debug.Send("i: " + i + ", j: " + j);

            if (i == r.length || j == content.length) {
//                Debug.Send("reached end");
                return i == r.length && j == content.length;
            }

            if (!matcher.match(r[i], content[j]))
                return false;

            //The offsets have to be the same, otherwise the shape isn't equal.
            if (iOffx != jOffx || iOffy != jOffy) return false;
        }
    }

    public static boolean shapeMatches(ItemStack[] content, ItemStack[] r, IMatcher<ItemStack> matcher){
        int rowSize = content == null ? 0 : (int)Math.sqrt(content.length);

        return shapeIterationMatches(content, r, matcher, rowSize) || shapeIterationMatches(mirror(content, rowSize), r, matcher, rowSize);
    }

    private static ItemStack[] ensureNoGaps(ItemStack[] items){
        return Arrays.asList(items).stream().filter(x -> x != null).toArray(ItemStack[]::new);
    }

    public static boolean ingredientsMatch(ItemStack[] a, ItemStack[] b, IMatcher<ItemStack> matcher){
        //array with all values to false.
        a = ensureNoGaps(a);
        b = ensureNoGaps(b);

        if(a.length == 0 || b.length == 0) return false;
        if(a.length != b.length) return false;

        //use no primitive type to allow Boolean stream of objects instead of arrays.
        Boolean[] used = new Boolean[a.length];
        Arrays.fill(used, false);

        for(ItemStack inRecipe : a){
            if(inRecipe == null) continue;
            //Look if inRecipe matches with an ingredient.
            for(int i = 0; i < used.length; i++) {
                if (used[i]) continue;
                if(b[i] == null){
                    Bukkit.getLogger().log(Level.SEVERE, "Error, found null ingredient.");
                    return false;
                }

                if(matcher.match(b[i], inRecipe)){
                    used[i] = true;
                    break;
                }
            }
        }
        return !Arrays.stream(used).anyMatch(x -> x == false);
    }

}
