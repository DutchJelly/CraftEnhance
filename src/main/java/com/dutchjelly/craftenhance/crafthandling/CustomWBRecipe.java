package com.dutchjelly.craftenhance.crafthandling;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

public class CustomWBRecipe {

    @Getter
    protected int id;

    @Getter @Setter
    private ItemStack result;

    @Getter @Setter
    private ItemStack[] content;

    @Getter @Setter
    private boolean shapeless;

    @Getter @Setter
    private boolean matchMeta;

    @Getter @Setter
    private String permissions;

}
