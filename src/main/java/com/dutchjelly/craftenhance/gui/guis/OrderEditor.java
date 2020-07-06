package com.dutchjelly.craftenhance.gui.guis;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.dutchjelly.craftenhance.IEnhancedRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OrderEditor extends GUIElement {

	private List<IEnhancedRecipe> recipes;
	private Inventory[] inventories;
	private int currentPage;
	
	public OrderEditor(GuiManager manager, GuiTemplate template, GUIElement previous, Player p){
		super(manager, template, previous, p);
		Debug.Send("An instance is being made for an order editor");
		recipes = new ArrayList<>(getManager().getMain().getFm().getRecipes());
        this.addBtnListener(ButtonType.NxtPage, this::handlePageChangingClicked);
        this.addBtnListener(ButtonType.PrvPage, this::handlePageChangingClicked);
        this.addBtnListener(ButtonType.SaveRecipe, this::handleSave);
		currentPage = 0;
		generateInventories();
	}

    private void handleSave(ItemStack itemStack, ButtonType buttonType) {

    }

    private void handlePageChangingClicked(ItemStack itemStack, ButtonType buttonType) {




    }

    //protected so a editor doesn't have to create a new instance when it deletes something and the user goes to previous page.
	protected void generateInventories(){

		int itemsPerPage = getTemplate().getFillSpace().size();
		int requiredPages = Math.max((int)Math.ceil((double)recipes.size()/itemsPerPage), 1);


		List<ItemStack> recipeItems = recipes.stream().map(x -> x.getResult()).collect(Collectors.toList());
		inventories = new Inventory[requiredPages];
		for(int i = 0; i < requiredPages; i++){
			inventories[i] = GuiUtil.FillInventory(
					GuiUtil.CopyInventory(getTemplate().getTemplate(), getTemplate().getInvTitle(), this),
					getTemplate().getFillSpace(),
					recipeItems.subList(itemsPerPage*i, Math.min(recipeItems.size(), itemsPerPage*(i+1)))
			);
		}

		if(currentPage >= inventories.length) currentPage = inventories.length-1;
	}
	
	@Override
	public Inventory getInventory() {
		if(currentPage >= inventories.length) currentPage = 0;
		return inventories[currentPage];
	}

	@Override
	public void handleEventRest(InventoryClickEvent e) {
		Messenger.Message("The order editor is not yet implemented.", e.getWhoClicked());
	}

    @Override
    public boolean isCancelResponsible() {
        return false;
    }

    private void move(IEnhancedRecipe recipe, int translation){
		int startIndex = translation > 0 ? 0 : -translation;
		int endIndex = translation > 0 ? recipes.size()-translation : recipes.size();
		for(int i = startIndex; i < endIndex; i++)
			if(recipes.get(i).equals(recipe)){
				switchRecipes(i, i + translation);
				return;
			}
	}
	
	private void switchRecipes(int a, int b){
		IEnhancedRecipe temp = recipes.get(a);
		recipes.set(a, recipes.get(b));
		recipes.set(b, temp);
	}
	
	private IEnhancedRecipe findResultingRecipe(ItemStack result, int clickPos){
		if(clickPos > getInventory().getSize()-9) return null;
		int translatedClickPos = currentPage * (getInventory().getSize()-9) + clickPos;
		if(translatedClickPos >= recipes.size()) return null;
		return recipes.get(translatedClickPos);
	}
	
	
	private void scroll(int amount){
		currentPage = Math.abs((currentPage + amount) % inventories.length);
	}

}

