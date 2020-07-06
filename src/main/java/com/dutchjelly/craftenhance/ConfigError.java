package com.dutchjelly.craftenhance;

import com.dutchjelly.craftenhance.messaging.Messenger;

public class ConfigError extends RuntimeException {

    public ConfigError(String message){
        super(message);
    }

    @Override
    public void printStackTrace(){
        Messenger.Error("(Configuration error) " + getMessage());
    }

}
