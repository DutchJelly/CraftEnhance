package com.dutchjelly.craftenhance.gui.guis;

import com.dutchjelly.craftenhance.ConfigError;
import com.dutchjelly.craftenhance.IEnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import com.dutchjelly.craftenhance.messaging.Debug;
import lombok.Lombok;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WBRecipeViewer extends GUIElement {

    private Inventory inventory;
    private WBRecipe recipe;

    public WBRecipeViewer(GuiManager manager, GuiTemplate template, GUIElement previous, Player p, WBRecipe recipe) {
        super(manager, template, previous, p);
        this.recipe = recipe;

        inventory = GuiUtil.CopyInventory(getTemplate().getTemplate(), getTemplate().getInvTitle(), this);
        updateRecipeDisplay();
        updatePlaceHolders();
    }

    private void updateRecipeDisplay(){
        List<Integer> fillSpace = getTemplate().getFillSpace();
        if (fillSpace.size() != 10)
            throw new ConfigError("fill space of WBrecipe viewer must be 10");
        for (int i = 0; i < 9; i++) {
            if (fillSpace.get(i) >= inventory.getSize())
                throw new ConfigError("fill space spot " + fillSpace.get(i) + " is outside of inventory");
            inventory.setItem(fillSpace.get(i), recipe.getContent()[i]);
        }
        if (fillSpace.get(9) >= inventory.getSize())
            throw new ConfigError("fill space spot " + fillSpace.get(9) + " is outside of inventory");
        inventory.setItem(fillSpace.get(9), recipe.getResult());
    }

    private void updatePlaceHolders(){
        List<Integer> fillSpace = getTemplate().getFillSpace();
        ItemStack[] template = getTemplate().getTemplate();
        Map<String, String> placeHolders = new HashMap<String,String>(){{
            put(InfoItemPlaceHolders.Key.getPlaceHolder(), recipe.getKey() == null ? "null" : recipe.getKey());
            put(InfoItemPlaceHolders.MatchMeta.getPlaceHolder(), recipe.isMatchMeta() ? "match meta" : "only match type");
            put(InfoItemPlaceHolders.Permission.getPlaceHolder(), recipe.getPermissions() == null ? "null" : recipe.getPermissions());
            put(InfoItemPlaceHolders.Shaped.getPlaceHolder(), recipe.isShapeless() ? "shapeless" : "shaped");
        }};

        for(int i = 0; i < template.length; i++){
            if(fillSpace.contains(i)) continue;
            if(template[i] == null) continue;
            inventory.setItem(i, GuiUtil.ReplaceAllPlaceHolders(template[i].clone(), placeHolders));
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void handleEventRest(InventoryClickEvent e) {
        int clickedSlot = e.getSlot();
        if (getTemplate().getFillSpace().contains(clickedSlot)) {
            int translatedSlot = -1;
            for (int i = 0; i < getTemplate().getFillSpace().size(); i++) {
                if (getTemplate().getFillSpace().get(i) == clickedSlot) {
                    translatedSlot = i;
                    break;
                }
            }
            if (translatedSlot == -1) return;

            IEnhancedRecipe clickedItemRecipe = getManager().getMain().getFm().getRecipes().stream().filter(x -> x.getResult().equals(e.getCurrentItem())).findFirst().orElse(null);
            if (clickedItemRecipe == null || clickedItemRecipe.equals(recipe)) return;

            if (clickedItemRecipe instanceof WBRecipe)
                getManager().openGUI(getPlayer(), new WBRecipeViewer(getManager(), getTemplate(), this, getPlayer(), (WBRecipe) clickedItemRecipe));
        }
    }

    @Override
    public boolean isCancelResponsible() {
        return false;
    }
}