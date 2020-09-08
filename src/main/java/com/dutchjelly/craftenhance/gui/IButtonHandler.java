package com.dutchjelly.craftenhance.gui;

import com.dutchjelly.craftenhance.gui.util.ButtonType;
import org.bukkit.inventory.ItemStack;

public interface IButtonHandler {
    void handleClick(ItemStack btn, ButtonType btnType);
}
