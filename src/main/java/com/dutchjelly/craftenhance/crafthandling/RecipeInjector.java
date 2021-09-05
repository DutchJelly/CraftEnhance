package com.dutchjelly.craftenhance.crafthandling;


import java.time.LocalDateTime;
import java.util.*;


import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;

import com.dutchjelly.craftenhance.api.CraftEnhanceAPI;
import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.RecipeType;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.crafthandling.util.*;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.util.Pair;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.Furnace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipeInjector implements Listener{
	
	private JavaPlugin plugin;
	private RecipeLoader loader;
	private boolean disableDefaultModeldataCrafts;

    //Stores info to pause furnaces from running their burn event every tick.
    private final Map<Furnace, LocalDateTime> pausedFurnaces = new HashMap<>();

    //Keep track of the id's of the owners of containers.
    @Getter
    private final Map<Location, UUID> containerOwners = new HashMap<>();
	
	public RecipeInjector(JavaPlugin plugin){
	    this.plugin = plugin;
		loader = RecipeLoader.getInstance();
		disableDefaultModeldataCrafts = plugin.getConfig().getBoolean("disable-default-custom-model-data-crafts");
	}

    //Add registrations of owners of containers.
    public void registerContainerOwners(Map<Location, UUID> containerOwners) {
        //Make sure to only register containers, in case some are non existent anymore.
        containerOwners.entrySet().forEach(entry -> {
            if(entry.getKey().getBlock() instanceof Container)
                this.containerOwners.put(entry.getKey(), entry.getValue());
        });
    }

    private boolean containsModeldata(CraftingInventory inv) {
        return Arrays.stream(inv.getMatrix()).anyMatch(x -> x != null && x.hasItemMeta() && x.getItemMeta().hasCustomModelData());
    }

    private IMatcher<ItemStack> getTypeMatcher() {
	    return Adapter.canUseModeldata() && disableDefaultModeldataCrafts ?
                ItemMatchers.constructIMatcher(ItemMatchers::matchType, ItemMatchers::matchModelData)
                : ItemMatchers::matchType;
    }

    @EventHandler
    public void handleCrafting(PrepareItemCraftEvent e){

	    if(e.getRecipe() == null || e.getRecipe().getResult() == null || !plugin.getConfig().getBoolean("enable-recipes")) return;
	    if(!(e.getInventory() instanceof CraftingInventory)) return;

	    CraftingInventory inv = e.getInventory();
	    Recipe serverRecipe = e.getRecipe();

        Debug.Send("The server wants to inject " + serverRecipe.getResult().toString() + " ceh will check or modify this.");

        List<RecipeGroup> possibleRecipeGroups = loader.findGroupsByResult(serverRecipe.getResult(), RecipeType.WORKBENCH);

        if(possibleRecipeGroups == null || possibleRecipeGroups.size() == 0) {
            if(disableDefaultModeldataCrafts && Adapter.canUseModeldata() &&  containsModeldata(inv)) {
                inv.setResult(null);
            }
            return;
        }

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
                    if(WBRecipeComparer.shapeMatches(content, inv.getMatrix(), getTypeMatcher())){
                        inv.setResult(sRecipe.getResult());
                        return;
                    }
                }else if(sRecipe instanceof ShapelessRecipe){
                    ItemStack[] ingredients = ServerRecipeTranslator.translateShapelessRecipe((ShapelessRecipe)sRecipe);
                    if(WBRecipeComparer.ingredientsMatch(ingredients, inv.getMatrix(), getTypeMatcher())){
                        inv.setResult(sRecipe.getResult());
                        return;
                    }
                }
            }
        }
        inv.setResult(null); //We found similar custom recipes, but none matched exactly. So set result to null.
    }

    public Optional<ItemStack> getFurnaceResult(ItemStack source, Furnace furnace) {
        ItemStack[] srcMatrix = new ItemStack[]{source};
	    FurnaceRecipe recipe = new FurnaceRecipe(null, null, srcMatrix);
        RecipeGroup group = RecipeLoader.getInstance().findSimilarGroup(recipe);
        if(group == null) {
            Debug.Send("furnace recipe does not match any group, so not changing the outcome");
            return null;
        }
        UUID playerId = containerOwners.get(furnace.getLocation());
        Player p = playerId == null ? null : plugin.getServer().getPlayer(playerId);
        Debug.Send("Furnace belongs to player with id: " + playerId);

        //Check if any grouped enhanced recipe is a match.
        for(EnhancedRecipe eRecipe : group.getEnhancedRecipes()){
            FurnaceRecipe fRecipe = (FurnaceRecipe) eRecipe;

            Debug.Send("Checking if enhanced recipe for " + fRecipe.getResult().toString() + " matches.");

            if(fRecipe.matches(srcMatrix)){
                if(entityCanCraft(p, fRecipe)) {
                    //TODO test if result can be changed here
                    Debug.Send("found enhanced recipe result for furnace");
                    return Optional.of(fRecipe.getResult());
                }
            }
            Debug.Send("found recipe doesn't match or no perms.");
        }
        //Check for similar server recipes if no enhanced ones match.
        for (Recipe sRecipe : group.getServerRecipes()) {
            org.bukkit.inventory.FurnaceRecipe fRecipe = (org.bukkit.inventory.FurnaceRecipe) sRecipe;
            if (getTypeMatcher().match(fRecipe.getInput(), source)) {
                Debug.Send("found similar server recipe for furnace");
                return Optional.of(fRecipe.getResult());
            }
        }
        return Optional.empty();
    }

    @EventHandler
    public void smelt(FurnaceSmeltEvent e){
	    Debug.Send("furnace smelt");
	    Optional<ItemStack> result = getFurnaceResult(e.getSource(), (Furnace)e.getBlock().getState());
	    if(result == null) return;

	    if(result.isPresent())
            e.setResult(result.get());
	    else e.setCancelled(true);

    }

    @EventHandler
    public void burn(FurnaceBurnEvent e){
	    if(e.isCancelled()) return;
        Furnace f = (Furnace)e.getBlock().getState();

        //Reduce computing time by pausing furnaces. This can be removed if we also check for hoppers
        //instead of only clicks to unpause.
        if(pausedFurnaces.getOrDefault(f, LocalDateTime.now()).isAfter(LocalDateTime.now())){
            e.setCancelled(true);
            return;
        }


        Debug.Send("furnace burn");
        Optional<ItemStack> result = getFurnaceResult(f.getInventory().getSmelting(), (Furnace)e.getBlock().getState());
        if(result != null && !result.isPresent()){
            e.setCancelled(true);
            pausedFurnaces.put(f, LocalDateTime.now().plusSeconds(10L));
        }
    }

    @EventHandler
    public void furnaceClick(InventoryClickEvent e) {
	    if(e.isCancelled()) return;
        if(e.getView().getTopInventory() instanceof FurnaceInventory) {
            Furnace f = (Furnace)e.getView().getTopInventory().getHolder();
            pausedFurnaces.remove(f);
        }
    }

    @EventHandler
    public void furnacePlace(BlockPlaceEvent e) {
        if(e.isCancelled()) return;
        if(e.getBlock().getType().equals(Material.FURNACE)) {
            containerOwners.put(e.getBlock().getLocation(), e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void furnaceBreak(BlockBreakEvent e) {
        if(e.isCancelled()) return;
        if(e.getBlock().getType().equals(Material.FURNACE)) {
            containerOwners.remove(e.getBlock().getLocation());
            pausedFurnaces.remove((Furnace)e.getBlock().getState());
        }
    }

    private boolean entityCanCraft(Permissible entity, EnhancedRecipe recipe){
	    return recipe.getPermissions() == null || recipe.getPermissions().equals("")
                || entity != null && entity.hasPermission(recipe.getPermissions());
    }
}
