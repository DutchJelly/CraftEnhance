package com.dutchjelly.craftenhance.crafthandling.recipes;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("FurnaceRecipe")
public class FurnaceRecipe extends EnhancedRecipe {

    static {
        ConfigurationSerialization.registerClass(FurnaceRecipe.class);
    }

    @Getter @Setter
    private int duration = -1;

    @Getter @Setter
    private float exp = 0;

    private FurnaceRecipe(Map<String, Object> args){
        super(args);
    }

    public FurnaceRecipe(String perm, ItemStack result, ItemStack[] content){
        super(perm, result, content);
    }

    public static FurnaceRecipe deserialize(Map<String, Object> args){
        FurnaceRecipe recipe = new FurnaceRecipe(args);
        recipe.duration = (short)args.get("duration");
        recipe.exp = (int)args.get("exp");
        return recipe;
    }

    @Override
    public Map<String, Object> serialize(){
        return new HashMap<String, Object>(){{
            putAll(FurnaceRecipe.super.serialize());
            put("exp", exp);
            put("duration", duration);
        }};
    }

    @Override
    public boolean matches(ItemStack[] content) {
        return content.length == 1 && (isMatchMeta() ? ItemMatchers.matchMeta(content[0], getContent()[0]) : ItemMatchers.matchTypeData(content[0], getContent()[0]));
    }

    @Override
    public Recipe getServerRecipe() {
        return Adapter.GetFurnaceRecipe(CraftEnhance.self(), ServerRecipeTranslator.GetFreeKey(getKey()), getResult(), getContent()[0].getType(), getDuration(), getExp());
    }

    @Override
    public boolean isSimilar(Recipe r) {
        if(!(r instanceof org.bukkit.inventory.FurnaceRecipe)) return false;
        org.bukkit.inventory.FurnaceRecipe serverRecipe = (org.bukkit.inventory.FurnaceRecipe)r;

        return ItemMatchers.matchType(serverRecipe.getInput(), getContent()[0])
                && ItemMatchers.matchType(serverRecipe.getResult(), getResult());
    }

    @Override
    public boolean isSimilar(EnhancedRecipe r) {
        return r instanceof FurnaceRecipe && ItemMatchers.matchTypeData(r.getContent()[0], getContent()[0]);
    }

    @Override
    public boolean isAlwaysSimilar(Recipe r) {
        if(!ItemMatchers.matchItems(r.getResult(), getResult()))
            return false;
        if(!(r instanceof org.bukkit.inventory.FurnaceRecipe))
            return false;
        return isSimilar(r);
    }
}
