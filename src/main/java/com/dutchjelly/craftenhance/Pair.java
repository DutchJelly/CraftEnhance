package com.dutchjelly.craftenhance;


import lombok.Getter;
import lombok.Setter;

//This is not accessible in the server-environment for some reason.
public class Pair<T,G> {

    @Getter @Setter
    private T first;

    @Getter @Setter
    private G second;

    public Pair(T first, G second){
        this.first = first;
        this.second = second;
    }
}
