package com.dutchjelly.craftenhance.crafthandling.recipes;

import com.dutchjelly.craftenhance.IEnhancedRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Map;

public class FurnaceRecipe implements IEnhancedRecipe {
    @Override
    public String getKey() {
        return null;
    }

    @Override
    public String getPermissions() {
        return null;
    }

    @Override
    public void setPermissions(String perms) {

    }

    @Override
    public ItemStack[] getContent() {
        return new ItemStack[0];
    }

    @Override
    public void setContent(ItemStack[] content) {

    }

    @Override
    public void setKey(String key) {

    }

    @Override
    public ItemStack getResult() {
        return null;
    }

    @Override
    public void setResult(ItemStack item) {

    }

    @Override
    public Recipe getServerRecipe() {
        return null;
    }

    @Override
    public boolean isSimilar(Recipe r) {
        return false;
    }

    @Override
    public boolean isAlwaysSimilar(Recipe r) {
        return false;
    }

    @Override
    public boolean isSimilar(IEnhancedRecipe r) {
        return false;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public void setHidden(boolean hidden) {

    }

    @Override
    public boolean isMatchMeta() {
        return false;
    }

    @Override
    public void setMatchMeta(boolean matchMeta) {

    }

    @Override
    public String validate() {
        return null;
    }

    @Override
    public int getPage() {
        return 0;
    }

    @Override
    public void setPage(int page) {

    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public void setSlot(int slot) {

    }

    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    public static FurnaceRecipe deserialize(Map<String, String> args){
        return null;
    }
}
