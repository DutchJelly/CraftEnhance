package com.dutchjelly.craftenhance.crafthandling;

import org.bukkit.inventory.ItemStack;

public interface Matcher {
    boolean match(ItemStack a, ItemStack b);
}
