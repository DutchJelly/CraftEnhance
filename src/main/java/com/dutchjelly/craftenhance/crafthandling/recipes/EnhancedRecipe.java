package com.dutchjelly.craftenhance.crafthandling.recipes;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import com.dutchjelly.craftenhance.crafthandling.util.WBRecipeComparer;
import com.dutchjelly.craftenhance.files.FileManager;
import com.dutchjelly.craftenhance.gui.interfaces.GuiPlacable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class EnhancedRecipe extends GuiPlacable implements ConfigurationSerializable, ServerLoadable {

    public EnhancedRecipe() { }

    public EnhancedRecipe(String perm, ItemStack result, ItemStack[] content){
        this.permissions = perm;
        this.result = result;
        this.content = content;
    }

    protected EnhancedRecipe(Map<String,Object> args){
        super(args);
        FileManager fm = CraftEnhance.self().getFm();

        List<String> recipeKeys;
        result = fm.getItem((String)args.get("result"));
        permissions = (String)args.get("permission");
        if(args.containsKey("matchtype")){
            matchType = ItemMatchers.MatchType.valueOf((String)args.get("matchtype"));
        } else if(args.containsKey("matchmeta")) {
            matchType = (Boolean) args.get("matchmeta") ?
                    ItemMatchers.MatchType.MATCH_META :
                    ItemMatchers.MatchType.MATCH_TYPE;
        }

        if(args.containsKey("oncraftcommand")) {
            onCraftCommand = (String)args.get("oncraftcommand");
        }

        if(args.containsKey("hidden"))
            hidden = (Boolean) args.get("hidden");


        recipeKeys = (List<String>)args.get("recipe");
        setContent(new ItemStack[recipeKeys.size()]);
        for(int i = 0; i < content.length; i++){
            content[i] = fm.getItem(recipeKeys.get(i));
        }
    }

    @Getter @Setter
    private int id;

    @Getter @Setter
    private String key;

    @Getter @Setter
    private ItemStack result;

    @Getter @Setter
    private ItemStack[] content;

    @Getter @Setter
    private ItemMatchers.MatchType matchType = ItemMatchers.MatchType.MATCH_META;

    @Getter @Setter
    private String permissions;

    @Getter @Setter
    private boolean hidden;

    @Getter @Setter
    private String onCraftCommand;

    @Getter
    private RecipeType type;

    @Override
    public Map<String, Object> serialize() {
        FileManager fm = CraftEnhance.getPlugin(CraftEnhance.class).getFm();
        return new HashMap<String, Object>(){{
            putAll(EnhancedRecipe.super.serialize());
            put("permission", permissions);
            put("matchtype", matchType.name());
            put("hidden", hidden);
            put("oncraftcommand", onCraftCommand);
            put("result", fm.getItemKey(result));
            put("recipe", Arrays.stream(content).map(x -> fm.getItemKey(x)).toArray(String[]::new));
        }};
    }

    public String validate(){
        if(result == null)
            return "recipe cannot have null result";
        if(!Adapter.canUseModeldata() && matchType == ItemMatchers.MatchType.MATCH_MODELDATA_AND_TYPE)
            return "recipe is using modeldata match while the server doesn't support it";
        if(content.length == 0 || !Arrays.stream(content).anyMatch(x -> x != null))
            return "recipe content cannot be empty";
        return null;
    }

    @Override
    public String toString(){
        String s = "";
        s += "key = " + key + "\n";
        s += "result = " + result == null ? "null" : result.toString() + "\n";
        return s;
    }

    @Override
    public ItemStack getDisplayItem(){
        return getResult();
    }

    public void save(){
        if(validate() == null)
            CraftEnhance.self().getFm().saveRecipe(this);
    }

    public void load(){
        RecipeLoader.getInstance().loadRecipe(this);
    }

    public abstract boolean matches(ItemStack[] content);
}
