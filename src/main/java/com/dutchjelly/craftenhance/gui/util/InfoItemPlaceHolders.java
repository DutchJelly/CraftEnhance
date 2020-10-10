package com.dutchjelly.craftenhance.gui.util;

public enum InfoItemPlaceHolders {
    MatchMeta("[match meta]"),
    Shaped("[shaped]"),
    Hidden("[hidden]"),
    Permission("[permission]"),
    Key("[key]"),
    Slot("[slot]"),
    DisableMode("[mode]"),
    Page("[page]");


    private String placeHolder;
    public String getPlaceHolder(){
        return placeHolder;
    }

    InfoItemPlaceHolders(String placeHolder){
        this.placeHolder = placeHolder;
    }
}
