package com.dutchjelly.craftenhance.gui.guis;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.util.CustomPrepareCraftEvent;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CustomCraftingTable extends GUIElement {

    private Inventory inventory;

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
            e.setCancelled(true);
            return;
        }
        Bukkit.getServer().getPluginManager().callEvent(new CustomPrepareCraftEvent(this));
    }

    @Override
    public void handleOutsideClick(InventoryClickEvent e){
        if(e.getCurrentItem() == null) return;

        //This makes sure that players can't shift click items into the wrong slots.
        if(!e.getClick().equals(ClickType.SHIFT_LEFT) && !e.getClick().equals(ClickType.SHIFT_RIGHT)){
            return;
        }

        Map<Integer, Integer> destination = GuiUtil.findDestination(e.getCurrentItem(), getInventory());

        if(destination.keySet().stream().anyMatch(x -> x != null && x != -1 && !getTemplate().getFillSpace().contains(x))){

            e.setCancelled(true);

            Bukkit.getScheduler().runTask(CraftEnhance.self(), () -> {
                ItemStack current = e.getCurrentItem().clone();
                if(destination.containsKey(-1))
                    e.getCurrentItem().setAmount(destination.get(-1));
                else e.setCurrentItem(null);

                destination.keySet().forEach(x -> {
                    if(getInventory().getItem(x) == null){
                        ItemStack fillItem = current.clone();
                        fillItem.setAmount(destination.get(x));
                        getInventory().setItem(x, fillItem);
                    }else{
                        getInventory().getItem(x).setAmount(destination.get(x) + getInventory().getItem(x).getAmount());
                    }
                });
            });

        }

        Bukkit.getServer().getPluginManager().callEvent(new CustomPrepareCraftEvent(this));
    }

    @Override
    public boolean isCancelResponsible() {
        return true;
    }

    public ItemStack getResult(){
        return inventory.getItem(getTemplate().getFillSpace().get(9));
    }

    public void setResult(ItemStack result){
        Bukkit.getScheduler().runTask(CraftEnhance.self(), () -> inventory.setItem(getTemplate().getFillSpace().get(9), result));
    }

    public ItemStack[] getMatrix(){
        return getTemplate().getFillSpace().subList(0,8).stream().map(x -> inventory.getItem(x)).toArray(ItemStack[]::new);
    }

}
