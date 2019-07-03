package com.dutchjelly.craftenhance.util;

import com.dutchjelly.craftenhance.messaging.Debug;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class RecipeUtil {

    //This class is only used for static utilities. Creating an instance
    //is illegal.
    public RecipeUtil() {
        throw new NotImplementedException();
    }

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
        ShapedRecipe shaped = new ShapedRecipe(craftRecipe.getResult());
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
                recipe.setIngredient((char) ('A' + i), content[i].getType());
            }
        }
    }
}
