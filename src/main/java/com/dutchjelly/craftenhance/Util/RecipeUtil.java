package com.dutchjelly.craftenhance.Util;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.model.CraftRecipe;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeUtil {

    //This class is only used for static utilities. Creating an instance
    //is illegal.
    public RecipeUtil() {
        throw new NotImplementedException();
    }

    private static List<String> UsedKeys = new ArrayList<>();

    //Formats content so that it's shifted to the left top.
    public static void Format(ItemStack[] content){
        if(IsNullArray(content)) return;
        boolean nullRow = true, nullColumn = true;
        while(nullRow || nullColumn){
            for(int i = 0; i < 3; i++){
                if(!IsNullElement(content[i]))
                    nullRow = false;
                if(!IsNullElement(content[i*3]))
                    nullColumn = false;
            }
            if(nullRow) ShiftUp(content);
            if(nullColumn) ShiftLeft(content);
        }
    }

    //Ensures that matrix has a default size of 9. Returns the ensured
    //array.
    public static ItemStack[] EnsureDefaultSize(ItemStack[] matrix){
        if(matrix.length == 9) return matrix;
        ItemStack[] defaultMatrix = new ItemStack[9];
        for(int i = 0; i < 9; i++){
            defaultMatrix[i] = null;
        }
        defaultMatrix[0] = matrix[0];
        defaultMatrix[1] = matrix[1];
        defaultMatrix[3] = matrix[2];
        defaultMatrix[4] = matrix[3];

        return defaultMatrix;
    }

    //Shifts content to the left one position.
    private static void ShiftLeft(ItemStack[] content){
        for(int i = 1; i < content.length; i++){
            if(i % 3 != 0){
                content[i-1] = content[i];
                content[i] = null;
            }
        }
    }

    //Shifts content to the top one position.
    private static void ShiftUp(ItemStack[] content){
        for(int i = 3; i < content.length; i++){
            content[i-3] = content[i];
            content[i] = null;
        }
    }

    //Prints content. Used for local debugging on Windows only!
    public static void PrintContent(ItemStack[] content){
        for(int i = 0; i < content.length; i++){
            if(content[i] == null) System.out.println(i + ": null");
            else System.out.println(i + ": " + content[i].getType());
        }
    }

    //Checks if content is an empty recipe.
    public static boolean IsNullArray(ItemStack[] content){
        for(int i = 0; i < content.length; i++){
            if(!IsNullElement(content[i])) return false;
        } return true;
    }

    //Checks if element is not a valid crafting material.
    public static boolean IsNullElement(ItemStack element){
        return element == null || element.getType().equals(Material.AIR);
    }


    //Shapes the craftRecipe that needs to be done to add it to the server.
    public static ShapedRecipe ShapeRecipe(CraftRecipe craftRecipe){
        ItemStack[] content = craftRecipe.getContents().clone();
        Format(content);
        //A key and namespace of the recipe on the server.
        String recipeKey = craftRecipe.getKey().toLowerCase().replaceAll("[^a-z0-9 ]", "");
        while(UsedKeys.contains(recipeKey)) recipeKey += "A";
        UsedKeys.add(recipeKey);
        ShapedRecipe shaped = Adapter.GetShapedRecipe(
                CraftEnhance.getPlugin(CraftEnhance.class), "ceh" + recipeKey, craftRecipe.getResult()
        );
        shaped.shape(GetShape(content));
        MapIngredients(shaped, content);
        return shaped;
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

    //Looks for every index if the item in recipe has an equal type of the item in content.
    public static boolean AreEqualTypes(ItemStack[] recipe, ItemStack[] content){
        if(recipe == null || content == null || recipe.length != content.length)
            return false;
        for(int i = 0; i < recipe.length; i++){
            if(!RecipeUtil.AreEqualTypes(content[i], recipe[i])) return false;
        }
        return true;
    }

    //Looks if the types of content and recipe match.
    public static boolean AreEqualTypes(ItemStack content, ItemStack recipe){
        content = EnsureNullAir(content);
        recipe = EnsureNullAir(recipe);
        if(content == null){
            return recipe == null;
        }
        return recipe != null && recipe.getType().equals(content.getType());
        //return recipe != null && Adapter.AreEqualTypes(recipe, content);
    }

    //Looks for every index if the item in recipe is equal to the item in content.
    public static boolean AreEqualItems(ItemStack[] recipe, ItemStack[] content){
        if(recipe == null || content == null || recipe.length != content.length)
            return false;
        for(int i = 0; i < recipe.length; i++){
            if(!RecipeUtil.AreEqualItems(content[i], recipe[i])){
//                Debug.Send("-------------------------------");
//                Debug.Send("Found two no equal items on index " + i + "...");
//                Debug.Send(content[i]);
//                Debug.Send(recipe[i]);
//                Debug.Send("-------------------------------");
                return false;
            }
        }
        return true;
    }

    //Looks if content and recipe are equal items.
    public static boolean AreEqualItems(ItemStack content, ItemStack recipe){
        content = EnsureNullAir(content);
        recipe = EnsureNullAir(recipe);
        return content == recipe || (content != null && recipe != null &&
                recipe.isSimilar(content));
    }

    //Used to counter changes in spigot versions: Inventories contain air instead of
    //null items in versions below 1.14.
    private static ItemStack EnsureNullAir(ItemStack item){
        if(item == null) return item;
        if(item.getType().equals(Material.AIR))
            return null;
        return item;
    }

    //Mirrors the content verticly. The length of content has to be 9 and should
    //represent a 3*3 crafting bench's content.
    public static ItemStack[] MirrorVerticle(ItemStack[] content){
        //012  ->  210
        //345  ->  543
        //678  ->  876
        if(content == null || content.length != 9) return null;
        ItemStack[] mirrored = new ItemStack[9];
        for(int i = 0; i < 9; i+=3){
            for(int j = 0; j < 3; j++){
                //Reverses the row for every loop of i.
                mirrored[i+2 - j] = content[i+j];
            }
        }
        //To verify that the mirroring is working. I should write a unit test for this.
        //Debug.Send("Mirrored \n" + String.join(", ", Arrays.asList(content).stream().map(x -> x == null ? "null" : x.toString()).collect(Collectors.toList())) + " to \n" + String.join(",", Arrays.asList(mirrored).stream().map(x -> x == null ? "null" : x.toString()).collect(Collectors.toList())));
        return mirrored;
    }

}
