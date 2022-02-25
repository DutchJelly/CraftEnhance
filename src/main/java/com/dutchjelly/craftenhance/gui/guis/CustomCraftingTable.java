package com.dutchjelly.craftenhance.gui.guis;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.util.CustomPrepareCraftEvent;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.messaging.Debug;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class CustomCraftingTable extends GUIElement {

    private Inventory inventory;

    private ItemStack[] recipeContent;

    private ItemStack result;

    public CustomCraftingTable(GuiManager manager, GuiTemplate template, GUIElement previous, Player p){
        super(manager,template,previous,p);
        inventory = GuiUtil.CopyInventory(getTemplate().getTemplate(), getTemplate().getInvTitle(), this);

    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void handleEventRest(InventoryClickEvent e) {
        if(!getTemplate().getFillSpace().contains(e.getSlot())){
            Debug.Send("player clicked outside recipe");
            e.setCancelled(true);
            return;
        }



        //Content slot clicked.
        if(!getTemplate().getFillSpace().get(9).equals(e.getSlot())){

            //Just to be sure, generally you'd never have a recipe with the same item as the result.
            if(e.getClick().equals(ClickType.DOUBLE_CLICK)){
                ItemStack collector = e.getView().getCursor();
                if(!GuiUtil.isNull(e.getView().getCursor()) && collector.isSimilar(result)){
                    e.setCancelled(true);
                    return;
                }
            }

            Debug.Send("player clicked inside recipe content");
            Bukkit.getScheduler().runTask(CraftEnhance.self(), () -> {
                //Call event in next tick (after the click has finished)
                Bukkit.getServer().getPluginManager().callEvent(new CustomPrepareCraftEvent(this));
            });
            return;
        }
        //Result slot clicked.
        Debug.Send("player clicked result slot");

        if(result == null && getResult() != null)
            throw new IllegalStateException("custom table has result set but no recipe in memory");


        if(result == null){
            Debug.Send("recipe is null");
            return;
        }

        e.setCancelled(true); //disable all default behaviour when handling result clicks

        int canCraft = 1; //Player can always craft 1.

        if(e.getClick().equals(ClickType.SHIFT_LEFT) || e.getClick().equals(ClickType.SHIFT_RIGHT)){
            //Assume that inventory is empty and map out the quantity that couldn't be mapped to a slot to find available space.
            Map<Integer, Integer> destination = GuiUtil.findDestination(
                    result, getPlayer().getInventory(),
                    getPlayer().getInventory().getContents().length*getResult().getMaxStackSize(),
                    false, null
            );
            int space = destination.keySet().stream().filter(x -> x != null && x != -1)
                    .mapToInt(x -> destination.get(x).intValue()).sum();
            canCraft = Math.min(findFittingRecipesAmount(), space);

            if(canCraft == 0){
                return;
            }

            int remainingRewardResults = canCraft;
            //Add results to inventory of player
            while(remainingRewardResults > 0){
                ItemStack resultClone = result.clone();
                int resultCloneAmount = Math.min(remainingRewardResults, resultClone.getMaxStackSize());
                resultClone.setAmount(resultCloneAmount);
                getPlayer().getInventory().addItem(resultClone);
                remainingRewardResults -= resultCloneAmount;
            }
        }else{
            ItemStack onCursor = getPlayer().getOpenInventory().getCursor();
            if(!GuiUtil.isNull(onCursor)) {
                Debug.Send("result has to add to nonempty cursor: " + onCursor.getType());
                if (!onCursor.isSimilar(result)){
                    return;
                }

                if (onCursor.getAmount() + result.getAmount() > onCursor.getMaxStackSize()){
                    return;
                }



                onCursor.setAmount(onCursor.getAmount() + result.getAmount());
            }else{
                Debug.Send("setting empty cursor to result item");
                getPlayer().getOpenInventory().setCursor(result.clone());
            }
        }

        //Subtract crafted amount from the matrix.
        //TODO: maybe add this in try catch finally block

        Debug.Send("Player is crafting " + canCraft + " recipes");

        setMatrix(subtractRecipeFromMatrix(canCraft));
        Bukkit.getServer().getPluginManager().callEvent(new CustomPrepareCraftEvent(this));
    }


    //Make sure that the item gets put in the crafting table properly if possible. Also start an event if the content
    //changed.
    @Override
    public void handleOutsideClick(InventoryClickEvent e){

        //TODO simulate default behaviour without grabbing result item
        if(e.getClick().equals(ClickType.DOUBLE_CLICK)){
            ItemStack collector = e.getView().getCursor();
            if(!GuiUtil.isNull(e.getView().getCursor()) && collector.isSimilar(result)){
                e.setCancelled(true);
                return;
            }
        }

        if(e.getCurrentItem() == null) return;



        //This makes sure that players can't shift click items into the wrong slots.
        if(!e.getClick().equals(ClickType.SHIFT_LEFT) && !e.getClick().equals(ClickType.SHIFT_RIGHT)){
            return;
        }
        Debug.Send("Player shift clicked item in own inventory");

        e.setCancelled(true);

        Map<Integer, Integer> destination = GuiUtil.findDestination(
                e.getCurrentItem(), getInventory(), -1, false,
                getTemplate().getFillSpace().subList(0, 9)
        );

        int leftovers = destination.getOrDefault(-1, 0);
        destination.remove(-1);

        //Inventory is full.
        if(destination.isEmpty())
            return;

        //Place all items inside the crafting inventory.
        destination.keySet().forEach(x -> {
            if(getInventory().getItem(x) == null){
                ItemStack fillItem = e.getCurrentItem().clone();
                fillItem.setAmount(destination.get(x));
                getInventory().setItem(x, fillItem);
            }else{
                getInventory().getItem(x).setAmount(destination.get(x) + getInventory().getItem(x).getAmount());
            }
        });

        //Remove item from player inventory or adjust amount.
        if(leftovers != 0)
            e.getCurrentItem().setAmount(leftovers);
        else e.setCurrentItem(null);

        //We don't have to wait for default behaviour, so don't wait for next tick.
        Bukkit.getServer().getPluginManager().callEvent(new CustomPrepareCraftEvent(this));
    }

    @Override
    public void handleDragging(InventoryDragEvent e){

        //TODO: simulate left click dragging (which distributes the items)

        List<Integer> validDrags = e.getRawSlots().stream().filter(x -> getTemplate().getFillSpace().subList(0,9).contains(x) || x >= inventory.getSize()).collect(Collectors.toList());

        boolean updatedMatrix = validDrags.stream().anyMatch(x -> getTemplate().getFillSpace().subList(0,9).contains(x));

        if(e.getRawSlots().size() == validDrags.size()){
            if(updatedMatrix)
                Bukkit.getScheduler().runTask(CraftEnhance.self(), () ->
                    Bukkit.getServer().getPluginManager().callEvent(new CustomPrepareCraftEvent(this))
                );
            return;
        }


        e.setCancelled(true);

        int dropped = 0;
        final int maxDrop = e.getOldCursor().getAmount();



        for(int valid : validDrags){
            if(dropped >= maxDrop)
                break;


            ItemStack validItem = e.getView().getItem(valid);
            if(GuiUtil.isNull(validItem)){
                ItemStack clone = e.getOldCursor().clone();
                clone.setAmount(1);
                e.getView().setItem(valid, clone);
            }else if(validItem.getAmount() != validItem.getMaxStackSize()){
                validItem.setAmount(validItem.getAmount()+1);
            }else continue;

            dropped++;
        }

        Debug.Send("player dropped " + dropped + " item");
        ItemStack newCursor = e.getOldCursor().clone();
        if(dropped == newCursor.getAmount())
            Bukkit.getScheduler().runTask(CraftEnhance.self(), () -> e.getView().setCursor(null));
        else {
            newCursor.setAmount(newCursor.getAmount() - dropped);
            Bukkit.getScheduler().runTask(CraftEnhance.self(), () -> e.getView().setCursor(newCursor));
        }

        if(updatedMatrix){
            Bukkit.getServer().getPluginManager().callEvent(new CustomPrepareCraftEvent(this));
        }

//        List<Integer> invalidDrags = e.getInventorySlots().stream().filter(x -> !getTemplate().getFillSpace().subList(0,9).contains(x) && x < inventory.getSize()).collect(Collectors.toList());
//
//        if(invalidDrags.size() == e.getInventorySlots().size()){
//            e.setCancelled(true);
//            return;
//        }
//
//
//        for(int invalid : invalidDrags){
//            if(GuiUtil.isNull(inventory.getItem(invalid)))
//                throw new IllegalStateException("player dragged to result slot, but it's empty somehow");
//
//            ItemStack invalidItem = inventory.getItem(invalid);
//            if(invalidItem.getAmount() == 1)
//                inventory.setItem(invalid, null);
//            else invalidItem.setAmount(invalidItem.getAmount()-1);
//        }
//
//        ItemStack cursor = e.getCursor();
//        if(GuiUtil.isNull(cursor)){
//            ItemStack properCursor = e.getOldCursor().clone();
//            properCursor.setAmount(invalidDrags.size());
//            e.setCursor(properCursor);
//        }else{
//            cursor.setAmount(cursor.getAmount()+invalidDrags.size());
//        }
    }

    //This assumes that the recipe matches
    private int findFittingRecipesAmount(){
        //Idea: try to subtract until shape changes
        int i = -1, j = -1;

        ItemStack[] a = getMatrix();
        ItemStack[] b = recipeContent;


        int maxAmount = 64; //temporary set to stack size
        while(true){
            while(++i < a.length && a[i] == null);
            while(++j < b.length && b[j] == null);

            if(i == a.length || j == b.length){
                if(i != a.length || j != b.length)
                    throw new IllegalStateException("recipe does not match, they don't have an equal amount of items");
                break;
            }


            if(a[i].getAmount() < b[j].getAmount())
                throw new IllegalStateException("recipe doesn't match the amount");

            maxAmount = Math.min(maxAmount, a[i].getAmount()/b[j].getAmount());
        }
        return maxAmount;
    }

    private ItemStack[] subtractRecipeFromMatrix(int amount){
        ItemStack[] a = Arrays.copyOf(getMatrix(), getMatrix().length);
        ItemStack[] b = recipeContent;
        int i = -1, j = -1;

        while(true) {
            while (++i < a.length && a[i] == null) ;
            while (++j < b.length && b[j] == null) ;

            if (i == a.length || j == b.length) {
                if (i != a.length || j != b.length)
                    throw new IllegalStateException("recipe does not match, they don't have an equal amount of items");
                break;
            }

            int newAmount = a[i].getAmount() - amount * b[j].getAmount();
            if (newAmount < 0)
                throw new IllegalStateException("cannot craft " + amount + " items");

            if (newAmount == 0)
                a[i] = null;
            else a[i].setAmount(newAmount);
        }
        return a;
    }

    //order shapeless recipe contents so it can be maximally subtracted as a shaped recipe. This assumes that the recipe
    //matches
    private ItemStack[] orderShapelessRecipeContent(final ItemStack[] content){

        //specify the mapping of the current element to matrix. so sortedIndexes[0] = 3 means that content[0] needs to
        //map to matrix[3]
        int[] sortedIndexes = new int[content.length];
        ItemStack[] matrix = getMatrix();

        for(int i = 0; i < content.length; i++){
            sortedIndexes[i] = -1;
            if(content[i] == null)
                continue;

            for(int j = 0; j < matrix.length; j++){
                final int k = j;
                if(Arrays.stream(sortedIndexes).anyMatch(x -> x == k))
                    continue;

                if(content[i].isSimilar(matrix[j]))
                    sortedIndexes[i] = j;
            }
            if(sortedIndexes[i] == -1)
                throw new IllegalStateException("could not convert shapeless recipe to shaped recipe");
        }

        ItemStack[] shaped = new ItemStack[matrix.length];
        Arrays.fill(shaped, null);
        for(int i = 0; i < sortedIndexes.length; i++){
            if(sortedIndexes[i] == -1)
                continue;
            shaped[sortedIndexes[i]] = content[i];
        }

        //now sort on quantity so we can craft a max amount with shaped
        boolean swabbed = true;
        while(swabbed){
            swabbed = false;

            for(int i = 0; i < shaped.length-1; i++){
                if(shaped[i] == null) continue;
                for(int j = i+1; j < shaped.length; j++){
                    if(!shaped[i].isSimilar(shaped[j])) continue;

                    boolean isLarger = shaped[i].getAmount() > shaped[j].getAmount();
                    boolean needsLarger = matrix[i].getAmount() > matrix[j].getAmount();

                    if((!isLarger && needsLarger) || (isLarger && !needsLarger)){
                        GuiUtil.swap(shaped, i, j);
                        swabbed = true;
                    }
                }
            }
        }
        return shaped;
    }

    @Override
    public boolean isCancelResponsible() {
        return true;
    }

    public ItemStack getResult(){
        return inventory.getItem(getTemplate().getFillSpace().get(9));
    }

    public void setRecipe(ItemStack[] content, ItemStack result, boolean shapeless){
        this.result = result;
        this.recipeContent = shapeless ? orderShapelessRecipeContent(content) : content;

        inventory.setItem(getTemplate().getFillSpace().get(9), result);
    }


    public ItemStack[] getMatrix(){
        return getTemplate().getFillSpace().subList(0,9).stream().map(x -> inventory.getItem(x)).toArray(ItemStack[]::new);
    }
    public void setMatrix(ItemStack[] matrix){
        for(int i = 0; i < matrix.length; i++){
            Debug.Send("setting item " + i + " to " + matrix[i]);
            getInventory().setItem(getTemplate().getFillSpace().get(i), matrix[i]);
        }
    }

}
