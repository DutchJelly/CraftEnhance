package com.dutchjelly.craftenhance.crafthandling;


import java.util.*;


import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;

import com.dutchjelly.craftenhance.api.CraftEnhanceAPI;
import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.crafthandling.util.CustomPrepareCraftEvent;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import com.dutchjelly.craftenhance.crafthandling.util.WBRecipeComparer;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.util.Pair;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Furnace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipeInjector implements Listener{
	
	private JavaPlugin plugin;
	private RecipeLoader loader;
	
	public RecipeInjector(JavaPlugin plugin){
	    this.plugin = plugin;
		loader = RecipeLoader.getInstance();
	}


    @EventHandler
    public void handleCrafting(PrepareItemCraftEvent e){

	    if(e.getRecipe() == null || e.getRecipe().getResult() == null || !plugin.getConfig().getBoolean("enable-recipes")) return;
	    if(!(e.getInventory() instanceof CraftingInventory)) return;

	    CraftingInventory inv = e.getInventory();
	    Recipe serverRecipe = e.getRecipe();

        Debug.Send("The server wants to inject " + serverRecipe.getResult().toString() + " ceh will check or modify this.");

        List<RecipeGroup> possibleRecipeGroups = loader.findGroupsByResult(serverRecipe.getResult());

        if(possibleRecipeGroups == null || possibleRecipeGroups.size() == 0) return;

        for(RecipeGroup group : possibleRecipeGroups){

            //Check if any grouped enhanced recipe is a match.
            for(EnhancedRecipe eRecipe : group.getEnhancedRecipes()){
                if(!(eRecipe instanceof WBRecipe)) return;

                WBRecipe wbRecipe = (WBRecipe)eRecipe;

                Debug.Send("Checking if enhanced recipe for " + wbRecipe.getResult().toString() + " matches.");

                if(wbRecipe.matches(inv.getMatrix())
                    && e.getViewers().stream().allMatch(x -> entityCanCraft(x, wbRecipe))
                    && !CraftEnhanceAPI.fireEvent(wbRecipe, (Player)e.getViewers().get(0), inv, group)){

                    inv.setResult(wbRecipe.getResult());
                    return;
                }
                Debug.Send("Recipe doesn't match.");
            }

            //Check for similar server recipes if no enhanced ones match.
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
                }
            }
        }
        inv.setResult(null); //We found similar custom recipes, but none matched exactly. So set result to null.
    }

    @SneakyThrows
    @EventHandler
    public void onCustomCraft(CustomPrepareCraftEvent e){
//	    Debug.Send("custom prepare craft fired, trying to inject recipe");
//	    Debug.Send(Arrays.stream(e.getTable().getMatrix()).map(x -> x == null ? "null" : x.getType().name()).collect(Collectors.joining(",")));
//        CustomCraftingTable table = e.getTable();
//	    RecipeLoader.getInstance().getLoadedRecipes().forEach(x -> {
//	        if(!(x instanceof EnhancedRecipe)) return;
//	        EnhancedRecipe wbRecipe = (EnhancedRecipe)x;
//	        if(wbRecipe.isShapeless() && WBRecipeComparer.ingredientsMatch(table.getMatrix(), wbRecipe.getContent(), wbRecipe.isMatchMeta() ? ItemMatchers::matchMeta : ItemMatchers::matchType)){
//                table.setRecipe(wbRecipe.getContent(), wbRecipe.getResult(), wbRecipe.isShapeless());
//                return;
//            }
//            if(!wbRecipe.isShapeless() && WBRecipeComparer.shapeMatches(table.getMatrix(), wbRecipe.getContent(), wbRecipe.isMatchMeta() ? ItemMatchers::matchMeta : ItemMatchers::matchType)){
//                table.setRecipe(wbRecipe.getContent(), wbRecipe.getResult(), wbRecipe.isShapeless());
//                return;
//            }
//        });
//
//        Player p = e.getTable().getPlayer();
//        CraftInventoryCrafting cic = (CraftInventoryCrafting) p.openWorkbench(null, true).getTopInventory();
//
//        Field hiddenPIField = CraftInventoryCrafting.class.getDeclaredField("resultInventory");
//        hiddenPIField.setAccessible(true);
//        IInventory pi = (IInventory)hiddenPIField.get(cic);
//
//        CraftInventoryPlayer cip = (CraftInventoryPlayer)pi;
//
////        PlayerInventory pi = (PlayerInventory)ii;
//
//        ContainerWorkbench cwb = null; //new ContainerWorkbench(1234, pi);
//        //ContainerWorkbench
//
//        p.openInventory(e.getTable().getInventory());
//        for(int i = 0; i < e.getTable().getMatrix().length; i++){
//            cwb.setItem(i, CraftItemStack.asNMSCopy(e.getTable().getMatrix()[i]));
//        }
//
//
//	    List<Recipe> defaultRecipes = RecipeLoader.getInstance().getServerRecipes();
//        for(Recipe sRecipe : defaultRecipes){
//            if(sRecipe instanceof ComplexRecipe){
//                Debug.Send("complex recipe found...");
//                CraftComplexRecipe ccr = (CraftComplexRecipe)sRecipe;
//                //IRecipeComplex irc =  null; //(IRecipeComplex)ccr;
//
//                Field hiddenIRCField = CraftComplexRecipe.class.getDeclaredField("recipe");
//                hiddenIRCField.setAccessible(true);
//                IRecipeComplex irc = (IRecipeComplex)hiddenIRCField.get(ccr);
//                Debug.Send(cwb.a(irc) ? "true" : "false");
//            }
//
//            CraftShapedRecipe
//
//            if(sRecipe instanceof ShapedRecipe){
//                ItemStack[] content = ServerRecipeTranslator.translateShapedRecipe((ShapedRecipe)sRecipe);
//                if(WBRecipeComparer.shapeMatches(content, table.getMatrix(), ItemMatchers::matchType)) {
//                    table.setRecipe(content, sRecipe.getResult(), false);
//                    return;
//                }
//
//
//            }else if(sRecipe instanceof ShapelessRecipe){
//                ItemStack[] ingredients = ServerRecipeTranslator.translateShapelessRecipe((ShapelessRecipe)sRecipe);
////                Debug.Send("checking " + Arrays.stream(ingredients).map(x -> x == null ? "null" : x.getType().name()).collect(Collectors.joining(",")));
//                if(WBRecipeComparer.ingredientsMatch(ingredients, table.getMatrix(), ItemMatchers::matchType)){
//                    table.setRecipe(ingredients, sRecipe.getResult(), true);
//                    return;
//                }
//            }else continue;
//        }
//
//        Debug.Send("no matching recipe in custom crafting table");
//        table.setRecipe(null, null, false);
    }


    //Track what player has last accessed a furnace, and use that as permissible.
    private Map<Location, Permissible> furnaceData = new HashMap<>();

	@EventHandler
    public void onFurnaceOpen(InventoryOpenEvent e){
	    if(!e.isCancelled() && e.getInventory() instanceof FurnaceInventory)
	        furnaceData.put(e.getInventory().getLocation(), e.getPlayer());
    }

    @EventHandler
    public void smelt(FurnaceSmeltEvent e){
	    Debug.Send("furnace smelt");
	    RecipeGroup group = RecipeLoader.getInstance().<FurnaceRecipe>findGroupBySource(e.getSource());
        if(group == null) return;

        boolean didMatchEnhanced = false;

        //Check if any grouped enhanced recipe is a match.
        for(EnhancedRecipe eRecipe : group.getEnhancedRecipes()){
            FurnaceRecipe fRecipe = (FurnaceRecipe) eRecipe;

            Debug.Send("Checking if enhanced recipe for " + fRecipe.getResult().toString() + " matches.");

            if(fRecipe.matches(new ItemStack[]{e.getSource()})){
                if(furnaceData.containsKey(e.getBlock().getLocation()) && entityCanCraft(furnaceData.get(e.getBlock().getLocation()), fRecipe)) {
                    //TODO test if result can be changed here
                    e.setResult(fRecipe.getResult());
                    return;
                }
                didMatchEnhanced = true;
            }

            Debug.Send("Recipe doesn't match or no perms.");
        }

        //Check for similar server recipes if no enhanced ones match.
        for(Recipe sRecipe : group.getServerRecipes()){
            org.bukkit.inventory.FurnaceRecipe fRecipe = (org.bukkit.inventory.FurnaceRecipe)sRecipe;
            if(ItemMatchers.matchType(fRecipe.getInput(), e.getSource())){
                e.setResult(fRecipe.getResult());
                return;
            }
        }

        if(didMatchEnhanced)
            e.setCancelled(true);
    }

//    @EventHandler
//    public void burn(FurnaceBurnEvent e){
//        Debug.Send("furnace burn");
//        Permissible permissible = furnaceData.get(e.getBlock().getLocation());
//        Furnace f = (Furnace)e.getBlock().getState();
//        ItemStack smelting = f.getInventory().getSmelting();
//    }
//
//    @EventHandler
//    public void click(InventoryClickEvent e){
//	    if(!(e.getView().getTopInventory() instanceof FurnaceInventory)) return;
//        Debug.Send("furnace click");
//
//        final FurnaceInventory inv = (FurnaceInventory)e.getView().getTopInventory();
//
//        Bukkit.getScheduler().runTaskLater(CraftEnhance.self(), () -> {
//            Furnace f = inv.getHolder();
//            f.setCookTimeTotal(-1);
//            f.update();
//            Debug.Send(f.getCookTimeTotal());
//            Debug.Send(f.getBurnTime());
//        }, 1L);
//    }

    private boolean entityCanCraft(Permissible entity, EnhancedRecipe recipe){
	    return recipe.getPermissions() == null || recipe.getPermissions().equals("")
                || entity.hasPermission(recipe.getPermissions());
    }
}
