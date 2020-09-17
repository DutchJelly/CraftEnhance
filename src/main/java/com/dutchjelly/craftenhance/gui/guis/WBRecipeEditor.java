package com.dutchjelly.craftenhance.gui.guis;

import com.dutchjelly.craftenhance.ConfigError;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WBRecipeEditor extends GUIElement {
	
	private Inventory inventory;
	private WBRecipe recipe;
	private String permission;
	private boolean matchMeta;
	private boolean shapeless;
	private boolean hidden;

	public WBRecipeEditor(GuiManager manager, GuiTemplate template, GUIElement previous, Player p, WBRecipe recipe){
	    super(manager,template,previous,p);
	    this.recipe = recipe;
        inventory = GuiUtil.CopyInventory(getTemplate().getTemplate(), getTemplate().getInvTitle(), this);

        addBtnListener(ButtonType.SaveRecipe, this::saveRecipe);
        addBtnListener(ButtonType.DeleteRecipe, this::deleteRecipe);
        addBtnListener(ButtonType.ChangeCategory, this::changeCategory);
        addBtnListener(ButtonType.SwitchMatchMeta, this::switchMatchMeta);
        addBtnListener(ButtonType.SwitchShaped, this::switchShaped);
        addBtnListener(ButtonType.ResetRecipe, this::resetRecipe);
        addBtnListener(ButtonType.SetPosition, this::setPosition);
        addBtnListener(ButtonType.SetPermission, this::setPermission);

        addBtnListener(ButtonType.SwitchHidden, this::switchHidden);

	    updateRecipeDisplay();
		updatePlaceHolders();
    }

    private void setPermission(ItemStack itemStack, ButtonType buttonType) {
        Messenger.Message("Please specify the permission. Write \"-\" for empty permission. Write \"&cQ&r\" to exit.", getPlayer());
        getManager().waitForChatInput(this, getPlayer(), this::handlePermissionSetCB);
    }

    private void handlePermissionSetCB(String message) {
        if(message == null || message.trim() == "") return;

        message = message.trim();

        if(message.toLowerCase().equals("q")) return;

        if(message.equals("-")){
            permission = "";
            return;
        }

        if(message.contains(" ")){
            Messenger.Message("A permission can't contain a space.", getPlayer());
            getManager().waitForChatInput(this, getPlayer(), this::handlePermissionSetCB);
            return;
        }

        permission = message;
        updatePlaceHolders();
    }

    private void switchHidden(ItemStack itemStack, ButtonType buttonType) {
        hidden = !hidden;
        updatePlaceHolders();
    }

    private void setPosition(ItemStack itemStack, ButtonType buttonType) {
		Messenger.Message("Please specify the page and slot that you want the recipe to be displayed on in format: \"&epage slot&r\". Write \"&cQ&r\" to exit.", getPlayer());
        getManager().waitForChatInput(this, getPlayer(), this::handlePositionChange);
	}

	public void handlePositionChange(String message){
	    if(message == null || message.trim() == "") return;

	    if(message.toLowerCase().equals("q")) return;

	    String args[] = message.split(" ");

	    if(args.length != 2) {
	        Messenger.Message("Please specify a page and slot number separated by a space.", getPlayer());
            getManager().waitForChatInput(this, getPlayer(), this::handlePositionChange);
            return;
        }
        int page = 0,slot = 0;
	    try{
	        page = Integer.parseInt(args[0]);
        }catch(NumberFormatException e){
	        Messenger.Message("Could not parse the page number.", getPlayer());
            getManager().waitForChatInput(this, getPlayer(), this::handlePositionChange);
            return;
        }

        try{
	        slot = Integer.parseInt(args[1]);
        }catch(NumberFormatException e){
            Messenger.Message("Could not parse the slot number.", getPlayer());
            getManager().waitForChatInput(this, getPlayer(), this::handlePositionChange);
            return;
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
    }



	private void updateRecipeDisplay(){
	    List<Integer> fillSpace = getTemplate().getFillSpace();
	    if(fillSpace.size() != 10)
	        throw new ConfigError("fill space of WBRecipeEditor must be 10");
        for(int i = 0; i < 9; i++){
            if(fillSpace.get(i) >= inventory.getSize())
                throw new ConfigError("fill space spot " + fillSpace.get(i) + " is outside of inventory");
            inventory.setItem(fillSpace.get(i), recipe.getContent()[i]);
        }
        if(fillSpace.get(9) >= inventory.getSize())
            throw new ConfigError("fill space spot " + fillSpace.get(9) + " is outside of inventory");
        inventory.setItem(fillSpace.get(9), recipe.getResult());
        matchMeta = recipe.isMatchMeta();
        shapeless = recipe.isShapeless();
        hidden = recipe.isHidden();
	}

	private void updatePlaceHolders(){
        List<Integer> fillSpace = getTemplate().getFillSpace();
        ItemStack[] template = getTemplate().getTemplate();
        Map<String, String> placeHolders = new HashMap<String,String>(){{
            put(InfoItemPlaceHolders.Key.getPlaceHolder(), recipe.getKey() == null ? "null" : recipe.getKey());
            put(InfoItemPlaceHolders.MatchMeta.getPlaceHolder(), matchMeta ? "match meta" : "only match type");
            put(InfoItemPlaceHolders.Hidden.getPlaceHolder(), hidden ? "hide recipe in menu" : "show recipe in menu");
            put(InfoItemPlaceHolders.Permission.getPlaceHolder(), permission == null || permission.trim().equals("") ? "null" : permission);
            put(InfoItemPlaceHolders.Shaped.getPlaceHolder(), shapeless ? "shapeless" : "shaped");
            put(InfoItemPlaceHolders.Slot.getPlaceHolder(), String.valueOf(recipe.getSlot()));
            put(InfoItemPlaceHolders.Page.getPlaceHolder(), String.valueOf(recipe.getPage()));
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

	private void switchShaped(ItemStack button, ButtonType btnType){
	    shapeless = !shapeless;
	    updatePlaceHolders();
    }

	private void switchMatchMeta(ItemStack button, ButtonType btnType){
	    matchMeta = !matchMeta;
	    updatePlaceHolders();
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
            if(viewer.recipes.removeIf(x -> x.getKey().equals(recipe.getKey())))
                viewer.generateInventories(null);
        }


	    getManager().openGUI(getPlayer(), getPreviousGui());


	}

	private void saveRecipe(ItemStack button, ButtonType btnType) {
	    if(getTemplate().getFillSpace().size() != 10) {
	        throw new ConfigError("Error, fill space size of wb recipe editor is not equal to 10.");
        }
	    if(getTemplate().getFillSpace().contains(null)) {
            throw new ConfigError("Error, fill space of wb recipe editor contains null element.");
        }

	    ItemStack newContents[] = getTemplate().getFillSpace().subList(0,9).stream().map(x -> {
	        ItemStack item = inventory.getItem(x);
	        if(item == null) return null;
	        if(item.getAmount() != 1) {
	            Messenger.Message("WBRecipes only support amounts of 1 in the recipe.", getPlayer());
	            item.setAmount(1);
            }
            return item;
        }).toArray(ItemStack[]::new);

		ItemStack newResult = inventory.getItem(getTemplate().getFillSpace().get(9));

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
		recipe.setMatchMeta(matchMeta);
		recipe.setShapeless(shapeless);
		recipe.setHidden(hidden);
		recipe.setPermissions(permission);
		getManager().getMain().getFm().saveRecipe(recipe);
		RecipeLoader.getInstance().loadRecipe(recipe);

		Messenger.Message("Successfully saved the recipe.", getPlayer());
		return;
	}


}
