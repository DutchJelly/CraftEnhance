package com.dutchjelly.craftenhance.exceptions;

import com.dutchjelly.craftenhance.messaging.Messenger;

public class RecipeError extends RuntimeException {

    public RecipeError(String message){
        super(message);
    }

    @Override
    public void printStackTrace(){
        Messenger.Error("(Recipe load error) " + getMessage());
    }
}
