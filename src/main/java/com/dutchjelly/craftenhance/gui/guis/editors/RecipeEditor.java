package com.dutchjelly.craftenhance.gui.guis.editors;

import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.exceptions.ConfigError;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.gui.guis.RecipesViewer;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RecipeEditor<RecipeT extends EnhancedRecipe> extends GUIElement {

    private Inventory inventory;

    @Getter
    private RecipeT recipe;

    private String permission;
    private boolean hidden;
    private ItemMatchers.MatchType matchType;

    public RecipeEditor(GuiManager manager, GuiTemplate template, GUIElement previous, Player p, RecipeT recipe){
        super(manager,template,previous,p);
        this.recipe = recipe;
        inventory = GuiUtil.CopyInventory(getTemplate().getTemplate(), getTemplate().getInvTitle(), this);

        addBtnListener(ButtonType.SaveRecipe, this::saveRecipe);
        addBtnListener(ButtonType.DeleteRecipe, this::deleteRecipe);
        addBtnListener(ButtonType.ChangeCategory, this::changeCategory);
        addBtnListener(ButtonType.SwitchMatchMeta, this::switchMatchMeta);
        addBtnListener(ButtonType.ResetRecipe, this::resetRecipe);
        addBtnListener(ButtonType.SetPosition, this::setPosition);
        addBtnListener(ButtonType.SetPermission, this::setPermission);
        addBtnListener(ButtonType.SwitchHidden, this::switchHidden);

        initBtnListeners();

        updateRecipeDisplay();
        updatePlaceHolders();
    }

    public RecipeEditor(GuiManager manager, GUIElement previous, Player p, RecipeT recipe){
        super(manager,previous,p);
        this.recipe = recipe;
        inventory = GuiUtil.CopyInventory(getTemplate().getTemplate(), getTemplate().getInvTitle(), this);

        addBtnListener(ButtonType.SaveRecipe, this::saveRecipe);
        addBtnListener(ButtonType.DeleteRecipe, this::deleteRecipe);
        addBtnListener(ButtonType.ChangeCategory, this::changeCategory);
        addBtnListener(ButtonType.SwitchMatchMeta, this::switchMatchMeta);
        addBtnListener(ButtonType.ResetRecipe, this::resetRecipe);
        addBtnListener(ButtonType.SetPosition, this::setPosition);
        addBtnListener(ButtonType.SetPermission, this::setPermission);
        addBtnListener(ButtonType.SwitchHidden, this::switchHidden);

        initBtnListeners();

        updateRecipeDisplay();
        updatePlaceHolders();
    }

    private void switchMatchMeta(ItemStack itemStack, ButtonType buttonType) {
        ItemMatchers.MatchType[] matchTypes = ItemMatchers.MatchType.values();
        int i;
        for(i = 0; i < matchTypes.length; i++) {
            if(matchTypes[i] == matchType) break;
        }
        if(i == matchTypes.length) {
            Debug.Send("couldn't find match type that's currently selected in the editor");
            return;
        }
        this.matchType = matchTypes[(i+1) % matchTypes.length];
        updatePlaceHolders();
    }

    protected abstract void initBtnListeners();

    private void setPermission(ItemStack itemStack, ButtonType buttonType) {
        Messenger.Message("Please specify the permission. Write \"-\" for empty permission. Write \"&cQ&r\" to exit.", getPlayer());
        getManager().waitForChatInput(this, getPlayer(), this::handlePermissionSetCB);
    }

    private boolean handlePermissionSetCB(String message) {
        if(message == null || message.trim() == "") return false;

        message = message.trim();

        if(message.toLowerCase().equals("q")) return false;

        if(message.equals("-")){
            permission = "";
            updatePlaceHolders();
            return false;
        }

        if(message.contains(" ")){
            Messenger.Message("A permission can't contain a space.", getPlayer());
            return true;
        }

        permission = message;
        updatePlaceHolders();
        return false;
    }

    private void switchHidden(ItemStack itemStack, ButtonType buttonType) {
        hidden = !hidden;
        updatePlaceHolders();
    }

    private void setPosition(ItemStack itemStack, ButtonType buttonType) {
        Messenger.Message("Please specify the page and slot that you want the recipe to be displayed on in format: \"&epage slot&r\". Write \"&cQ&r\" to exit.", getPlayer());
        getManager().waitForChatInput(this, getPlayer(), this::handlePositionChange);
    }

    public boolean handlePositionChange(String message){
        if(message == null || message.trim() == "") return false;

        if(message.toLowerCase().equals("q")) return false;

        String args[] = message.split(" ");

        if(args.length != 2) {
            Messenger.Message("Please specify a page and slot number separated by a space.", getPlayer());
            return true;
        }
        int page = 0,slot = 0;
        try{
            page = Integer.parseInt(args[0]);
        }catch(NumberFormatException e){
            Messenger.Message("Could not parse the page number.", getPlayer());
            return true;
        }

        try{
            slot = Integer.parseInt(args[1]);
        }catch(NumberFormatException e){
            Messenger.Message("Could not parse the slot number.", getPlayer());
            return true;
        }

        recipe.setPage(page);
        recipe.setSlot(slot);

        Messenger.Message("Set the page to " + page + ", and the slot to " + slot + ". This will get auto-filled if it's not available.", getPlayer());
        getManager().getMain().getFm().saveRecipe(recipe);

        //Modify the previous GUI so the new position is rendered.
        if(getPreviousGui() instanceof RecipesViewer){
            RecipesViewer viewer = (RecipesViewer)getPreviousGui();
            viewer.generateInventories(getPlayer());
        }

        updatePlaceHolders();
        return false;
    }


    protected abstract void onRecipeDisplayUpdate();

    //Sets the current display of the recipe
    private void updateRecipeDisplay(){
        List<Integer> fillSpace = getTemplate().getFillSpace();
        if(fillSpace.size() != recipe.getContent().length+1)
            throw new ConfigError("fill space of Recipe editor must be " + (recipe.getContent().length+1) );
        for(int i = 0; i < recipe.getContent().length; i++){
            if(fillSpace.get(i) >= inventory.getSize())
                throw new ConfigError("fill space spot " + fillSpace.get(i) + " is outside of inventory");
            inventory.setItem(fillSpace.get(i), recipe.getContent()[i]);
        }
        if(fillSpace.get(recipe.getContent().length) >= inventory.getSize())
            throw new ConfigError("fill space spot " + fillSpace.get(recipe.getContent().length) + " is outside of inventory");
        inventory.setItem(fillSpace.get(recipe.getContent().length), recipe.getResult());
        matchType = recipe.getMatchType();
        hidden = recipe.isHidden();
        onRecipeDisplayUpdate();
    }

    protected abstract Map<String,String> getPlaceHolders();

    protected void updatePlaceHolders(){
        List<Integer> fillSpace = getTemplate().getFillSpace();
        ItemStack[] template = getTemplate().getTemplate();
        Map<String, String> placeHolders = new HashMap<String,String>(){{
            put(InfoItemPlaceHolders.Key.getPlaceHolder(), recipe.getKey() == null ? "null" : recipe.getKey());
            put(InfoItemPlaceHolders.MatchMeta.getPlaceHolder(), matchType.getDescription());
            put(InfoItemPlaceHolders.MatchType.getPlaceHolder(), matchType.getDescription());
            put(InfoItemPlaceHolders.Hidden.getPlaceHolder(), hidden ? "hide recipe in menu" : "show recipe in menu");
            put(InfoItemPlaceHolders.Permission.getPlaceHolder(), permission == null || permission.trim().equals("") ? "none" : permission);
            put(InfoItemPlaceHolders.Slot.getPlaceHolder(), String.valueOf(recipe.getSlot()));
            put(InfoItemPlaceHolders.Page.getPlaceHolder(), String.valueOf(recipe.getPage()));
        }};

        placeHolders.putAll(getPlaceHolders());

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
        if(getTemplate().getFillSpace().contains(e.getSlot()))
            return;
        e.setCancelled(true);
    }

    @Override
    public boolean isCancelResponsible() {
        return true;
    }

    private void changeCategory(ItemStack button, ButtonType btnType){
//        throw new NotImplementedException("That operation is not yet implemented.");
        Messenger.Message("That's not implemented yet.", getPlayer());
    }

    private void resetRecipe(ItemStack button, ButtonType btnType){
        updateRecipeDisplay();
    }

    private void deleteRecipe(ItemStack button, ButtonType btnType) {

        getManager().getMain().getFm().removeRecipe(recipe);
        RecipeLoader.getInstance().unloadRecipe(recipe);


        //Modify the previous GUI so it doesn't show the deleted recipe.
        if(getPreviousGui() instanceof RecipesViewer){
            RecipesViewer viewer = (RecipesViewer)getPreviousGui();
            if(viewer.getRecipes().removeIf(x -> x.getKey().equals(recipe.getKey())))
                viewer.generateInventories(null);
        }

        getManager().openGUI(getPlayer(), getPreviousGui());
    }

    private void saveRecipe(ItemStack button, ButtonType btnType) {
        if(getTemplate().getFillSpace().size() != recipe.getContent().length+1) {
            throw new ConfigError("Error, fill space size of wb recipe editor is not equal to 10.");
        }
        if(getTemplate().getFillSpace().contains(null)) {
            throw new ConfigError("Error, fill space of wb recipe editor contains null element.");
        }

        ItemStack newContents[] = getTemplate().getFillSpace().subList(0,recipe.getContent().length).stream().map(x -> {
            ItemStack item = inventory.getItem(x);
            if(item == null) return null;
            if(item.getAmount() != 1) {
                Messenger.Message("Recipes only support amounts of 1 in the content.", getPlayer());
                item.setAmount(1);
            }
            return item;
        }).toArray(ItemStack[]::new);

        ItemStack newResult = inventory.getItem(getTemplate().getFillSpace().get(recipe.getContent().length));

        if(!Arrays.stream(newContents).anyMatch(x -> x != null)){
            Messenger.Message("The recipe is empty.", getPlayer());
            return;
        }

        if(newResult == null){
            Messenger.Message("The result slot is empty.", getPlayer());
            return;
        }
        recipe.setContent(newContents);
        recipe.setResult(newResult);

        recipe.setMatchType(matchType);
        recipe.setHidden(hidden);
        beforeSave();
        recipe.setPermissions(permission);
        recipe.save();
        recipe.load();

        Messenger.Message("Successfully saved the recipe.", getPlayer());
        return;
    }

    protected abstract void beforeSave();


}
