package com.dutchjelly.craftenhance;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.List;
import java.util.Map;

public interface IEnhancedRecipe extends ConfigurationSerializable {
    String getKey();
    String getPermissions();
    void setPermissions(String perms);
    ItemStack[] getContent();
    void setKey(String key);
    ItemStack getResult();
    Recipe getServerRecipe();
    boolean isSimilar(Recipe r);
    boolean isAlwaysSimilar(Recipe r);
    boolean isSimilar(IEnhancedRecipe r);
    boolean isHidden();

    String validate();

    int getPage();
    void setPage(int page);
    int getSlot();
    void setSlot(int slot);




    String toString();
    Map<String, Object> serialize();


}
