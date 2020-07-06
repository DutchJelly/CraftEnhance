package com.dutchjelly.craftenhance.gui.util;

import org.bukkit.inventory.ItemStack;

public interface IButtonHandler {
    void handleClick(ItemStack btn, ButtonType btnType);
}
