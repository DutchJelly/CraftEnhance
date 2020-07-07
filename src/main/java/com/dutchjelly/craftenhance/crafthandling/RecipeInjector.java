package com.dutchjelly.craftenhance.crafthandling;


import java.util.*;


import com.dutchjelly.craftenhance.IEnhancedRecipe;

import com.dutchjelly.craftenhance.api.CraftEnhanceAPI;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import com.dutchjelly.craftenhance.crafthandling.util.WBRecipeComparer;
import com.dutchjelly.craftenhance.messaging.Debug;
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

    private Map<CraftingInventory, PrepareItemCraftEvent> prepareLock = new HashMap<>();
	
	public RecipeInjector(JavaPlugin plugin){
	    this.plugin = plugin;
		loader = RecipeLoader.getInstance();
	}



    @EventHandler
    public void handleCrafting(PrepareItemCraftEvent e){





	    if(e.getRecipe() == null || e.getRecipe().getResult() == null || !plugin.getConfig().getBoolean("enable-recipes")) return;

	    if(!(e.getInventory() instanceof CraftingInventory)) return;

	    CraftingInventory inv = e.getInventory();
        if(prepareLock.containsKey(inv)) {
            prepareLock.put(inv, e);
            return;
        }
        Debug.Send(">>>>>>>>>Prepare craft");

	    Recipe serverRecipe = e.getRecipe();

        Debug.Send("The server wants to inject " + serverRecipe.getResult().toString() + " ceh will check or modify this.");

        List<RecipeGroup> possibleRecipeGroups = loader.findGroupsByResult(serverRecipe.getResult());

        if(possibleRecipeGroups == null || possibleRecipeGroups.size() == 0) return;

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
                        return;
                    }
                }else{
                    if(!e.getViewers().stream().allMatch(x -> entityCanCraft(x, wbRecipe)))
                        continue;

                    //Look if the recipe's amount is already reduced, meaning that we can inject at amount of 1.
                    if(awaitingSingleCraft.containsKey(inv) && awaitingSingleCraft.get(inv).equals(wbRecipe)){
                        Debug.Send("recipe was awaiting single craft");
                        inv.setResult(wbRecipe.getResult());
                        awaitingSingleCraft.remove(inv);
                        injectedRecipes.remove(inv);
                        return;
                    }

                    if(WBRecipeComparer.shapeMatches(inv.getMatrix(), wbRecipe.getContent(), wbRecipe.isMatchMeta() ? ItemMatchers::matchMetaAndAmount : ItemMatchers::matchTypeAndAmount)){
                        if(CraftEnhanceAPI.fireEvent(wbRecipe, (Player)e.getViewers().get(0), inv, group))
                            continue;
                        inv.setResult(wbRecipe.getResult());
                        injectedRecipes.put(inv, wbRecipe);
                        Debug.Send("put recipe in injectedRecipes map");
                        return;
                    }else{
                        Debug.Send("shape doesn't match");
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
        inv.setResult(null); //We found similar custom recipes, but none matched exactly. So set result to null.
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
	    if(e.getView().getTopInventory() instanceof CraftingInventory){
	        CraftingInventory inv = (CraftingInventory)e.getView().getTopInventory();
	        if(injectedRecipes.containsKey(inv))
	            injectedRecipes.remove(inv);
        }
    }


    @EventHandler
    public void onClick(InventoryClickEvent e){
        Debug.Send(">>>>>>>>>Click");
	    if(e.isCancelled())
	        return;
	    if(!(e.getClickedInventory() instanceof CraftingInventory))
	        return;
	    if(!e.getSlotType().equals(InventoryType.SlotType.RESULT))
	        return;

        CraftingInventory inv = (CraftingInventory)e.getClickedInventory();
        prepareLock.put(inv, null);

        if(injectedRecipes.containsKey(inv) && injectedRecipes.get(inv) instanceof WBRecipe){
            WBRecipe recipe = (WBRecipe)injectedRecipes.get(inv);
            if(e.getClick().equals(ClickType.SHIFT_LEFT)){
                final int craftAmount = Math.min(getMaxCraftAmount(inv, recipe), space(recipe.getResult(), e.getWhoClicked()));
                Debug.Send("can craft " + craftAmount);
                ensureQuantityRemoval(inv, recipe, craftAmount);
                inv.setMatrix(Arrays.stream(inv.getMatrix()).map(x -> {
                    if(x == null) return null;
                    int newAmount = x.getAmount()-craftAmount;
                    if(newAmount <= 0) return null;
                    if(newAmount < 0) Debug.Send("warning... negative amount..");
                    x.setAmount(newAmount);
                    return x;
                }).toArray(ItemStack[]::new));
                int totalResultItems = recipe.getResult().getAmount() * craftAmount;
                int maxStackSize = recipe.getResult().getMaxStackSize();
                while(totalResultItems > 0){
                    ItemStack reward = recipe.getResult().clone();
                    reward.setAmount(Math.min(totalResultItems, maxStackSize));
                    e.getWhoClicked().getInventory().addItem(reward);
                    totalResultItems -= maxStackSize;
                }
                return;
            }
            ensureQuantityRemoval(inv, recipe, 1);
            injectedRecipes.remove(inv);
            Debug.Send("removed recipe from injected recipes");
            awaitingSingleCraft.put(inv, recipe);
            Debug.Send("recipe now is awaiting single craft");
        }

        if(prepareLock.get(inv) != null){
            PrepareItemCraftEvent cEvent = prepareLock.get(inv);
            prepareLock.remove(inv);
            handleCrafting(cEvent);
        }

    }

    private int getMaxCraftAmount(CraftingInventory inv, WBRecipe r){
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

    private int space(ItemStack item, HumanEntity h){
	    if(item == null) return 0;
	    int space = 0;
	    final int stackSize = item.getMaxStackSize();

	    for(ItemStack x : h.getInventory().getStorageContents()){
	        if(x == null)
	            space += stackSize;
            else if(x.isSimilar(item))
                space += (stackSize - x.getAmount());
        }
        return space/item.getAmount();
    }

    private void ensureQuantityRemoval(CraftingInventory inv, WBRecipe r, int amount){
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

            int remove = (rc[i].getAmount()-1) * amount;
            int newAmount = ic[j].getAmount()-remove;
            if(newAmount <= 0)
                throw new IllegalStateException("a recipe was injected while there were not enough items");
            ic[j].setAmount(newAmount);
        }
        inv.setMatrix(ic);
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
