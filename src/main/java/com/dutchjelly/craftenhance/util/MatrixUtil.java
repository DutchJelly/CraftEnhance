package com.dutchjelly.craftenhance.util;

public class MatrixUtil {

    public static void shiftUp(Object[] array, int width, int height){
        
    }

    public static void removeRow(Object[] array, int width, int height, int row){
        int startingIndex = width * row;
        for(int i = startingIndex; i < startingIndex+width; i++)
            array[i] = null;
    }

    public static void removeColumn(Object[] array, int width, int height, int column){
        for(int i = 0; i < array.length; i+= width)
            array[i] = null;
    }
}
