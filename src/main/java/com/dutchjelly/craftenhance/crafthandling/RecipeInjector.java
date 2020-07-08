package com.dutchjelly.craftenhance.crafthandling;


import java.util.*;


import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.IEnhancedRecipe;

import com.dutchjelly.craftenhance.api.CraftEnhanceAPI;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import com.dutchjelly.craftenhance.crafthandling.util.WBRecipeComparer;
import com.dutchjelly.craftenhance.messaging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipeInjector implements Listener{
	
	private JavaPlugin plugin;
	private RecipeLoader loader;

	private Map<CraftingInventory, IEnhancedRecipe> injectedRecipes = new HashMap<>();
	private Map<CraftingInventory, IEnhancedRecipe> awaitingSingleCraft = new HashMap<>();
    private Map<CraftingInventory, PrepareItemCraftEvent> craftingInjectQueue = new HashMap<>();
    private Map<CraftingInventory, PrepareItemCraftEvent> nonMatchingEvents = new HashMap<>();
	
	public RecipeInjector(JavaPlugin plugin){
	    this.plugin = plugin;
		loader = RecipeLoader.getInstance();
	}


    @EventHandler
    public void handleCrafting(PrepareItemCraftEvent e){

	    if(e.getRecipe() == null || e.getRecipe().getResult() == null || !plugin.getConfig().getBoolean("enable-recipes")) return;

	    if(!(e.getInventory() instanceof CraftingInventory)) return;

	    CraftingInventory inv = e.getInventory();
        if(craftingInjectQueue.containsKey(inv)) {
            craftingInjectQueue.put(inv, e);
            Debug.Send("waiting for click event to finish");
            return;
        }

	    Recipe serverRecipe = e.getRecipe();

        Debug.Send("The server wants to inject " + serverRecipe.getResult().toString() + " ceh will check or modify this.");

        List<RecipeGroup> possibleRecipeGroups = loader.findGroupsByResult(serverRecipe.getResult());

        if(possibleRecipeGroups == null || possibleRecipeGroups.size() == 0) return;



        //If the crafting inventory is awaiting to single craft, just set the result.
        if(awaitingSingleCraft.containsKey(inv)){
            Debug.Send("recipe was awaiting single craft");
            inv.setResult(awaitingSingleCraft.get(inv).getResult());
            awaitingSingleCraft.remove(inv);
            injectedRecipes.remove(inv);
            return;
        }


        for(RecipeGroup group : possibleRecipeGroups){
            for(IEnhancedRecipe eRecipe : group.getEnhancedRecipes()){
                if(!(eRecipe instanceof WBRecipe)) return;

                WBRecipe wbRecipe = (WBRecipe)eRecipe;

                Debug.Send("Checking if enhanced recipe for " + wbRecipe.getResult().toString() + " matches");

                if(wbRecipe.isShapeless()){
                    if(!e.getViewers().stream().allMatch(x -> entityCanCraft(x, wbRecipe)))
                        continue;

                    if(WBRecipeComparer.ingredientsMatch(inv.getMatrix(), wbRecipe.getContent(), wbRecipe.isMatchMeta() ? ItemMatchers::matchMeta : ItemMatchers::matchType)){

                        if(CraftEnhanceAPI.fireEvent(wbRecipe, (Player)e.getViewers().get(0), inv, group))
                            continue;
                        inv.setResult(wbRecipe.getResult());
                        injectedRecipes.put(inv, wbRecipe);
                        nonMatchingEvents.remove(inv);
                        return;
                    }
                }else{
                    if(!e.getViewers().stream().allMatch(x -> entityCanCraft(x, wbRecipe)))
                        continue;

                    if(WBRecipeComparer.shapeMatches(inv.getMatrix(), wbRecipe.getContent(), wbRecipe.isMatchMeta() ? ItemMatchers::matchMetaAndAmount : ItemMatchers::matchTypeAndAmount)){
                        if(CraftEnhanceAPI.fireEvent(wbRecipe, (Player)e.getViewers().get(0), inv, group))
                            continue;
                        inv.setResult(wbRecipe.getResult());
                        injectedRecipes.put(inv, wbRecipe);
                        nonMatchingEvents.remove(inv);
                        return;
                    }
                }
            }
            for(Recipe sRecipe : group.getServerRecipes()){
                if(sRecipe instanceof ShapedRecipe){
                    ItemStack[] content = ServerRecipeTranslator.translateShapedRecipe((ShapedRecipe)sRecipe);
                    if(WBRecipeComparer.shapeMatches(content, inv.getMatrix(), ItemMatchers::matchType)){
                        inv.setResult(sRecipe.getResult());
                        return;
                    }
                }else if(sRecipe instanceof ShapelessRecipe){
                    ItemStack[] ingredients = ServerRecipeTranslator.translateShapelessRecipe((ShapelessRecipe)sRecipe);
                    if(WBRecipeComparer.ingredientsMatch(ingredients, inv.getMatrix(), ItemMatchers::matchType)){
                        inv.setResult(sRecipe.getResult());
                        return;
                    }
                }else continue;
            }
        }
        injectedRecipes.put(inv, null);
        Debug.Send("no match, so setting null");
        nonMatchingEvents.put(inv, e);
        inv.setResult(null); //We found similar custom recipes, but none matched exactly. So set result to null.
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
	    if(e.getView().getTopInventory() instanceof CraftingInventory){
	        CraftingInventory inv = (CraftingInventory)e.getView().getTopInventory();
            injectedRecipes.remove(inv);
	        awaitingSingleCraft.remove(inv);
            craftingInjectQueue.remove(inv);
            nonMatchingEvents.remove(inv);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
	    if(e.isCancelled())
	        return;
	    if(!(e.getClickedInventory() instanceof CraftingInventory))
	        return;
        CraftingInventory inv = (CraftingInventory)e.getClickedInventory();

        //make sure that any injecting events wait for this event to finish with the inventory
        craftingInjectQueue.put(inv, null);

        if(e.getSlotType().equals(InventoryType.SlotType.RESULT) && injectedRecipes.containsKey(inv) && injectedRecipes.get(inv) instanceof WBRecipe){
            WBRecipe recipe = (WBRecipe)injectedRecipes.get(inv);
            if(e.getClick().equals(ClickType.SHIFT_LEFT)){
                final int craftAmount = Math.min(findFittingRecipesAmount(inv, recipe), findAvailableSpace(e.getWhoClicked().getInventory(), recipe.getResult()));
                subtractRecipe(inv, recipe, craftAmount, 0);
                addToInventory(e.getWhoClicked().getInventory(), craftAmount, recipe.getResult());
                return;
            }
            subtractRecipe(inv, recipe, 1, 1);
            injectedRecipes.remove(inv);
            awaitingSingleCraft.put(inv, recipe);
        }

        PrepareItemCraftEvent callback = craftingInjectQueue.remove(inv);
        if(callback != null){
            handleCrafting(callback);
        }

        if(nonMatchingEvents.containsKey(inv)){
            PrepareItemCraftEvent retry = nonMatchingEvents.remove(inv);
            Bukkit.getScheduler().scheduleSyncDelayedTask(CraftEnhance.self(), () -> {
                Debug.Send("retrying to inject the recipe");
                retry.getInventory().setMatrix(inv.getMatrix());
                Bukkit.getPluginManager().callEvent(retry);
            }, 1L);
        }
    }

    private void addToInventory(Inventory inv, int amount, ItemStack item){
	    if(item == null || inv == null) return;
	    final int maxStackSize = item.getMaxStackSize();
	    int totalItems = item.getAmount() * amount;

	    while(totalItems > 0){
	        ItemStack add = item.clone();
	        add.setAmount(Math.min(totalItems, maxStackSize));
	        inv.addItem(add);
	        totalItems -= maxStackSize;
        }
    }



    private int findAvailableSpace(Inventory inv, ItemStack item){
	    if(item == null || inv == null) return 0;
	    int space = 0;
	    final int stackSize = item.getMaxStackSize();

	    for(ItemStack x : inv.getStorageContents()){
	        if(x == null)
	            space += stackSize;
            else if(x.isSimilar(item))
                space += (stackSize - x.getAmount());
        }
        return space/item.getAmount();
    }

    //Removes the items needed to craft amount recipes of r from inv, where baseAmount is the amount that will
    //get handled by another event. This is 1 for the PrepareCraftEvent.
    private boolean subtractRecipe(CraftingInventory inv, WBRecipe r, int amount, int baseAmount){
	    ItemStack[] rc = r.getContent();
	    ItemStack[] ic = inv.getMatrix();
	    int i = -1, j = -1;
	    while(true){
            i = nextItemIndex(rc, i);
            j = nextItemIndex(ic, j);

            if(i >= rc.length || j >= ic.length){
                if(i != rc.length || j != ic.length)
                    throw new IllegalStateException("the amount of items in ensuring quantity removal didn't match");

                break;
            }

            if(ic[j] == null || rc[i] == null)
                throw new IllegalStateException("encountered a null item after going to next nonnull item");

            int remove = (rc[i].getAmount()-baseAmount) * amount;
            int newAmount = ic[j].getAmount()-remove;

            if(newAmount < 0){
                return false;
            }

            if(newAmount == 0) ic[j] = null;
            else ic[j].setAmount(newAmount);
        }
        inv.setMatrix(ic);
        return true;
    }

    private int findFittingRecipesAmount(CraftingInventory inv, WBRecipe r){
        ItemStack[] rc = r.getContent();
        ItemStack[] ic = inv.getMatrix();
        int i = -1, j = -1;

        int maxAmount = 64; //temporary set to stack size
        while(true){
            i = nextItemIndex(rc, i);
            j = nextItemIndex(ic, j);

            if(i >= rc.length || j >= ic.length){
                if(i != rc.length || j != ic.length)
                    throw new IllegalStateException("the amount of items in ensuring quantity removal didn't match");
                break;
            }

            if(ic[j] == null || rc[i] == null)
                throw new IllegalStateException("encountered a null item after going to next nonnull item");

            maxAmount = Math.min(maxAmount, ic[j].getAmount()/rc[i].getAmount());
        }
        return maxAmount;
    }

    private int nextItemIndex(ItemStack[] items, int current){
	    while(++current < items.length && items[current] == null);
	    return current;
    }

    private boolean entityCanCraft(HumanEntity entity, IEnhancedRecipe recipe){
	    return recipe.getPermissions() == null || recipe.getPermissions() == ""
                || entity.hasPermission(recipe.getPermissions());
    }
}
