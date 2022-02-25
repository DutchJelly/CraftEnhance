package com.dutchjelly.craftenhance.gui.guis.editors;

import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;


public class FurnaceRecipeEditor extends RecipeEditor<FurnaceRecipe> {

    public FurnaceRecipeEditor(GuiManager manager, GuiTemplate template, GUIElement previous, Player p, FurnaceRecipe recipe){
        super(manager, template, previous, p, recipe);
    }

    public FurnaceRecipeEditor(GuiManager manager, GUIElement previous, Player p, FurnaceRecipe recipe){
        super(manager, previous, p, recipe);
    }

    private int duration;
    private float exp;

    @Override
    public void initBtnListeners() {
        addBtnListener(ButtonType.SetCookTime, (btn, type) -> {
            Messenger.Message("Please input a cook duration.", getPlayer());
            this.getManager().waitForChatInput(this, getPlayer(), (msg) -> {
                short parsed;
                try{
                    parsed = Short.valueOf(msg);
                }catch(NumberFormatException e){
                    Messenger.Message("Error, you didn't input a number.", getPlayer());
                    return true;
                }
                if(parsed < 0) parsed = 0;
                Messenger.Message("Successfully set duration to " + parsed, getPlayer());
                duration = parsed;
                updatePlaceHolders();
                return false;
            });
        });
        addBtnListener(ButtonType.SetExp, (btn, type) -> {
            Messenger.Message("Please input an exp amount.", getPlayer());
            this.getManager().waitForChatInput(this, getPlayer(), (msg) -> {
                int parsed;
                try{
                    parsed = Integer.valueOf(msg);
                }catch(NumberFormatException e){
                    Messenger.Message("Error, you didn't input a number.", getPlayer());
                    return true;
                }
                if(parsed < 0) parsed = 0;
                Messenger.Message("Successfully set exp to " + parsed, getPlayer());
                exp = parsed;
                updatePlaceHolders();
                return false;
            });
        });
    }

    @Override
    public void onRecipeDisplayUpdate() {
        duration = getRecipe().getDuration();
        exp = getRecipe().getExp();
    }

    @Override
    public Map<String, String> getPlaceHolders() {
        return new HashMap<String, String>(){{
            put(InfoItemPlaceHolders.Exp.getPlaceHolder(), String.valueOf(exp));
            put(InfoItemPlaceHolders.Duration.getPlaceHolder(), String.valueOf(duration));
        }};
    }

    @Override
    public void beforeSave() {
        getRecipe().setDuration(duration);
        getRecipe().setExp(exp);
    }
}
