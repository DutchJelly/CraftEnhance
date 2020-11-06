package com.dutchjelly.craftenhance.util;

import com.dutchjelly.craftenhance.CraftEnhance;

public enum PermissionTypes {
    Edit("perms.recipe-editor"),
    View("perms.recipe-viewer"),
    EditItem("perms.edit-item");

    public final String permPath;

    PermissionTypes(String permPath){
        this.permPath = permPath;
    }

    public String getPerm(){
        return CraftEnhance.self().getConfig().getString(permPath);
    }
}
