package com.dutchjelly.craftenhance.gui.util;

public enum InfoItemPlaceHolders {
    MatchMeta("[match meta]"),
    Shaped("[shaped]"),
    Permission("[permission]"),
    Key("[key]"),
    Slot("[slot]"),
    Page("[page]");

    private String placeHolder;
    public String getPlaceHolder(){
        return placeHolder;
    }

    InfoItemPlaceHolders(String placeHolder){
        this.placeHolder = placeHolder;
    }
}
