package com.dutchjelly.craftenhance.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryCreator {
	
	//There are errors in the function getNextValidIndex because index 0 gets skipped I think.
	//Also there must be errors in increasing the pageindex
	
	private String name;
	private int size;
	List<Inventory> inventories;
	
	int maxIndexX, minIndexX, maxIndexY, minIndexY;
	
	public InventoryCreator(String name, int size){
		inventories = new ArrayList<Inventory>();
		this.name = name;
		this.size = size;
	}
	
	public void setMaxIndexes(int maxX, int minX, int maxY, int minY){
		maxIndexX = maxX;
		minIndexX = minX;
		maxIndexY = maxY;
		minIndexY = minY;
	}
	
	public void initInventory(int amountPages){
		if(amountPages < 0) return;
		for(int i = 0; i < amountPages; i++)
			inventories.add(Bukkit.createInventory(null, size, name));
	}
	
	public void addItems(List<ItemStack> items){
		int currentPage = 0, index = -1;
		for(ItemStack item : items){
			index = getNextValidIndex(index);
			if(!isBetweenBoundaries(index)){
				currentPage++;
				index = getNextValidIndex(-1);
			}
			inventories.get(currentPage).setItem(index, item);
		}
	}
	
	public void addItems(ItemStack item){
		int room = getRoomOnPage();
		int index = 0;
		for(Inventory inv : inventories){
			for(int i = 0; i < room; i++){
				index = getNextValidIndex(index);
				inv.setItem(index, item);
			}
		}
	}
	
	private int getNextValidIndex(int index){
		while(!isBetweenBoundaries(index++)){
			if(!fitsPage(index))
				return index;
		}
		return index;
	}
	
	private boolean fitsPage(int index){
		return index >= size;
	}
	
	private int getRoomOnPage(){
		return (maxIndexX-minIndexX+1) * (maxIndexY-minIndexY+1);
	}
	
	private boolean isBetweenBoundaries(int index){
		return index >= minIndexX && index <= maxIndexX 
			&& index >= minIndexY * 9 && index <= maxIndexY * 9;
	}
	
	public void setItems(Map<Integer, ItemStack> itemPositions){
		inventories.forEach(x -> itemPositions.keySet().forEach(y -> x.setItem(y, itemPositions.get(y))));
	}
	
	public List<Inventory> getInventories(){
		return inventories;
	}
	
	public Inventory getInventory(){
		if(inventories.isEmpty()) return null;
		return inventories.get(0);
	}
	
	
	
}
