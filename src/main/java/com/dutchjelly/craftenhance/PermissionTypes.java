package com.dutchjelly.craftenhance;

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
