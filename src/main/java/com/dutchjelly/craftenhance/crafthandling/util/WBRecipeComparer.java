package com.dutchjelly.craftenhance.crafthandling.util;

import com.dutchjelly.craftenhance.IMatcher;
import com.dutchjelly.craftenhance.messaging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class WBRecipeComparer {

    private static ItemStack[] mirror(ItemStack[] content){
        if(content == null) return null;
        if(content.length == 0) return content;
        ItemStack[] mirrored = new ItemStack[content.length];
        double dimensions = Math.sqrt(content.length);
        if(Math.ceil(dimensions) != Math.floor(dimensions)) return content;

        int size = (int)dimensions;

        for(int i = 0; i < size; i++){
            for(int j = 0; j < size/2; j++){
                mirrored[i*3+j] = content[i*3+(size-j-1)];
                mirrored[i*3+(size-j-1)] = content[i*3+j];
            }
            if(size%2 != 0)
                mirrored[i*3+(size/2)] = content[i*3+(size/2)];
        }
        return mirrored;
    }

    //This compares shapes and doesn't take mirrored recipes into account.
    //public for testing purposes. Not very professional I know, but it gets the job done.
    public static boolean shapeIterationMatches(ItemStack[] content, ItemStack[] r, IMatcher<ItemStack> matcher){
        //Find the first element of r and content.
        int i = -1, j = -1;
        while(++i < r.length && r[i] == null);
        while(++j < content.length && content[j] == null);

        //Look if one or both recipes are empty. Return true if both are empty.
        if(i == r.length || j == content.length) return i == r.length && j == content.length;

//        Debug.Send(Arrays.stream(content).map(x -> x == null ? "null" : x.toString()).collect(Collectors.joining(", ")));
//        Debug.Send(Arrays.stream(r).map(x -> x == null ? "null" : x.toString()).collect(Collectors.joining(", ")));
//
//        Debug.Send("i: " + i + ", j: " + j);

        if(!matcher.match(r[i],content[j])){
//            Debug.Send(r[i].toString());
//            Debug.Send(content[j].toString());
//            Debug.Send(String.valueOf(content[j].isSimilar(r[i])));
            return false;
        }

        int iOffx, iOffy, jOffx, jOffy;
        for(;;) {
            iOffx = iOffy = 0;
            jOffx = jOffy = 0;
            while (++i < r.length && r[i] == null) {
                iOffx++;
                if (i % 3 == 0) iOffy++;
            }
            if (i % 3 == 0) iOffy++;

            while (++j < content.length && content[j] == null) {
                jOffx++;
                if (j % 3 == 0) jOffy++;
            }
            if (j % 3 == 0) jOffy++;

//            Debug.Send("i: " + i + ", j: " + j);

            if (i == r.length || j == content.length) {
//                Debug.Send("reached end");
                return i == r.length && j == content.length;
            }

            if (!matcher.match(r[i], content[j]))
                return false;

            if (iOffx != jOffx || iOffy != jOffy) return false;
        }
    }

    public static boolean shapeMatches(ItemStack[] content, ItemStack[] r, IMatcher<ItemStack> matcher){
        return shapeIterationMatches(content, r, matcher) || shapeIterationMatches(mirror(content), r, matcher);
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
