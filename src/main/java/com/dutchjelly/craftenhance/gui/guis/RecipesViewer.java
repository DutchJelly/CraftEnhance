package com.dutchjelly.craftenhance.gui.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dutchjelly.craftenhance.IEnhancedRecipe;
import com.dutchjelly.craftenhance.PermissionTypes;

import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.messaging.Debug;

import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RecipesViewer extends GUIElement {

	protected List<IEnhancedRecipe> recipes;

	//Positions of format (itemsPerPage*pageNumber) + (index of slot in fill-space).
	private Map<Integer, IEnhancedRecipe> recipePositions;

    private Inventory[] inventories;
    private int currentPage = 0;
	//TODO: implement map for recipe location mapping to allow customizable recipe locations. I'm thinking of making that a config thing in a RecipesViewer GuiTemplate.

	public RecipesViewer(GuiManager manager, GuiTemplate template, GUIElement previous, Player p, List<IEnhancedRecipe> recipes){
        super(manager, template, previous, p);
	    Debug.Send("An instance is being made for a recipes viewer");
	    this.recipes = recipes;
	    this.addBtnListener(ButtonType.NxtPage, this::handlePageChangingClicked);
	    this.addBtnListener(ButtonType.PrvPage, this::handlePageChangingClicked);
        generateInventories(null);
	}

	//protected so a editor doesn't have to create a new instance when it deletes something and the user goes to previous page.
	protected void generateInventories(Player subscriber){

        int itemsPerPage = getTemplate().getFillSpace().size();
        int requiredPages = Math.max((int)Math.ceil((double)recipes.size()/itemsPerPage), 1);
        //We need more pages if statically positioned recipes are placed at a higher page index.
        requiredPages = Math.max(requiredPages, recipes.stream().map(x -> x.getPage()).max(Integer::compare).orElse(0)+1);

        List<Integer> fillSpace = getTemplate().getFillSpace();

        recipePositions = new HashMap<>();
        List<IEnhancedRecipe> notPositioned = new ArrayList<>();


        //Look to position statically positioned recipes.
        for (IEnhancedRecipe recipe : recipes) {
            if(recipe.getSlot() == -1 || recipe.getPage() == -1){
                notPositioned.add(recipe);
                continue;
            }
            if(!fillSpace.contains(recipe.getSlot())){
                Messenger.Message("Could not position recipe with key " + recipe.getKey() + " because it's outside of the fill space.", subscriber);
                notPositioned.add(recipe);
                continue;
            }

            int location = recipe.getPage()*itemsPerPage + fillSpace.indexOf(recipe.getSlot());
            if(recipePositions.containsKey(location)){
                Messenger.Message("Could not position recipe with key " + recipe.getKey() + " because another recipe has the same location", subscriber);
                notPositioned.add(recipe);
                continue;
            }
            recipePositions.put(location, recipe);
        }

        //Position non-positioned recipes by just increasing the indexes until one is not taken.
        int notPositionedIndex = 0, currentPosition = -1;
        while(notPositionedIndex < notPositioned.size()){
            while(recipePositions.containsKey(++currentPosition));
            recipePositions.put(currentPosition, notPositioned.get(notPositionedIndex++));
        }

        inventories = new Inventory[requiredPages];
        int titledInventories = getTemplate().getInvTitles().size();
        for(int i = 0; i < requiredPages; i++) {
            inventories[i] = GuiUtil.CopyInventory(getTemplate().getTemplate(), getTemplate().getInvTitles().get(i % titledInventories), this);
        }

        //It does get quite confusing here! It's mapping the locations to the actual locations specified in fill-space.
        for (int position : recipePositions.keySet()) {
            inventories[position/itemsPerPage].setItem(fillSpace.get(position%itemsPerPage), recipePositions.get(position).getResult());
        }

        //Check if current-page is not outside the bounds in case a recipe is removed.
        if(currentPage >= inventories.length) currentPage = inventories.length-1;
	}

    private void handlePageChangingClicked(ItemStack btn, ButtonType btnType){
        int direction = btnType == ButtonType.PrvPage ? -1 : 1;
        currentPage += direction;
        if(currentPage < 0) currentPage = inventories.length-1;
        else if(currentPage >= inventories.length) currentPage = 0;
        getManager().openGUI(getPlayer(), this);
    }

    @Override
	public Inventory getInventory() {
		return inventories[currentPage];
	}

	@Override
	public void handleEventRest(InventoryClickEvent e) {
        int clickedSlot = e.getSlot();
        List<Integer> fillSpace = getTemplate().getFillSpace();
        if(!fillSpace.contains(clickedSlot))
            return;

        int clickedRecipePosition = currentPage*fillSpace.size() + fillSpace.indexOf(clickedSlot);
        if(!recipePositions.containsKey(clickedRecipePosition))
            return;
        handleRecipeClick(recipePositions.get(clickedRecipePosition), e.getClick());
	}

	private void handleRecipeClick(IEnhancedRecipe clickedRecipe, ClickType clickType){
	    Debug.Send("handling recipe click..");
	    if((clickType == ClickType.MIDDLE || clickType == ClickType.RIGHT) && getPlayer().hasPermission(PermissionTypes.Edit.getPerm())){
	        if(clickedRecipe instanceof WBRecipe)
                getManager().openGUI(getPlayer(), new WBRecipeEditor(getManager(),getManager().getMain().getGuiTemplatesFile().getTemplate(WBRecipeEditor.class), this, getPlayer(), (WBRecipe)clickedRecipe));
            else Debug.Send("Could not find the class of a clicked recipe.");
            return;
	    }
        if(clickedRecipe instanceof WBRecipe)
            getManager().openGUI(getPlayer(), new WBRecipeViewer(getManager(),getManager().getMain().getGuiTemplatesFile().getTemplate(WBRecipeViewer.class), this, getPlayer(), (WBRecipe)clickedRecipe));
        else Debug.Send("Could not find the class of a clicked recipe.");
    }

	@Override
	public boolean isCancelResponsible() {
		return false;
	}


	public void setPage(int page){
	    if(page < 0) page = 0;
	    currentPage = Math.min(page, inventories.length-1);
    }
}
