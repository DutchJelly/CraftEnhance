package com.dutchjelly.craftenhance.gui.guis;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import com.dutchjelly.craftenhance.messaging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecipeDisabler extends GUIElement {

    //Reference to objects managed outside this gui.
    protected List<Recipe> enabledRecipes;
    protected List<Recipe> disabledRecipe;

    private Map<Integer, Recipe> placedRecipes = new HashMap<>();

    //If true, you can enable *disabled* recipes.
    boolean enableMode = false;

    private Inventory[] inventories;
    private int currentPage = 0;
    //TODO: implement map for recipe location mapping to allow customizable recipe locations. I'm thinking of making that a config thing in a RecipesViewer GuiTemplate.

    public RecipeDisabler(GuiManager manager, GuiTemplate template, GUIElement previous, Player p, List<Recipe> enabledRecipes, List<Recipe> disabledRecipes){
        super(manager, template, previous, p);
        Debug.Send("An instance is being made for a recipe disabler");
        this.enabledRecipes = enabledRecipes;
        this.disabledRecipe = disabledRecipes;
        this.addBtnListener(ButtonType.NxtPage, this::handlePageChangingClicked);
        this.addBtnListener(ButtonType.PrvPage, this::handlePageChangingClicked);
        this.addBtnListener(ButtonType.SwitchDisablerMode, this::switchMode);
        generateInventories(null);
        updatePlaceHolders();
    }

    private void switchMode(ItemStack itemStack, ButtonType buttonType) {
        this.enableMode = !this.enableMode;
        this.currentPage = 0;
        generateInventories(null);
        updatePlaceHolders();
        getManager().openGUI(getPlayer(), this);
    }

    private void updatePlaceHolders(){
        List<Integer> fillSpace = getTemplate().getFillSpace();
        ItemStack[] template = getTemplate().getTemplate();
        Map<String, String> placeHolders = new HashMap<String,String>(){{
            put(InfoItemPlaceHolders.DisableMode.getPlaceHolder(), enableMode ? "enable recipes by clicking them" : "disable recipes by clicking them");
        }};

        for(int i = 0; i < template.length; i++){
            if(fillSpace.contains(i)) continue;
            if(template[i] == null) continue;
            getInventory().setItem(i, GuiUtil.ReplaceAllPlaceHolders(template[i].clone(), placeHolders));
        }
    }

    private List<Recipe> getRecipes(){
        return !enableMode ? enabledRecipes : disabledRecipe;
    }

    //protected so a editor doesn't have to create a new instance when it deletes something and the user goes to previous page.
    protected void generateInventories(Player subscriber){

        int itemsPerPage = getTemplate().getFillSpace().size();
        int requiredPages = Math.max((int)Math.ceil((double)getRecipes().size()/itemsPerPage), 1);
        //We need more pages if statically positioned recipes are placed at a higher page index.

        List<Integer> fillSpace = getTemplate().getFillSpace();

        inventories = new Inventory[requiredPages];
        int titledInventories = getTemplate().getInvTitles().size();
        int recipeIndex = 0;

        for(int i = 0; i < requiredPages; i++) {
            inventories[i] = GuiUtil.CopyInventory(getTemplate().getTemplate(), getTemplate().getInvTitles().get(i % titledInventories), this);
            for(int spot : fillSpace){
                if(recipeIndex >= getRecipes().size()) break;

                ItemStack result = getRecipes().get(recipeIndex++).getResult();
                if(GuiUtil.isNull(result)) {
                    result = new ItemStack(Material.BARRIER);
                    ItemMeta meta = result.getItemMeta();
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&4Complex Recipe: " + Adapter.GetRecipeIdentifier(getRecipes().get(recipeIndex-1))));
                    meta.setLore(Arrays.asList("&eWARN: &fThis recipe is complex, which", "&f means that the result is only known", " &f&oafter&r&f the content of the crafting table is sent", " &fto the server. Think of repairing or coloring recipes.", " &f&nSo disabling is not recommended!"));
                    meta.setLore(meta.getLore().stream().map(x -> ChatColor.translateAlternateColorCodes('&', x)).collect(Collectors.toList()));
                    result.setItemMeta(meta);
                }else{
                    ItemMeta meta = result.getItemMeta();
                    meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&3key: &f" + Adapter.GetRecipeIdentifier(getRecipes().get(recipeIndex-1)))));
                    result.setItemMeta(meta);
                }
                inventories[i].setItem(spot, result);
                placedRecipes.put(i*inventories[i].getSize() + spot, getRecipes().get(recipeIndex-1));
            }
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

        int clickedRecipePosition = currentPage*getInventory().getSize() + clickedSlot;
        if(!placedRecipes.containsKey(clickedRecipePosition))
            return;
        Recipe recipe = placedRecipes.get(clickedRecipePosition);



       if(enableMode){
           if(RecipeLoader.getInstance().enableServerRecipe(recipe)){
               getInventory().setItem(clickedSlot, null);
//               getRecipes().remove(recipe);
           }

       }else{
           if(RecipeLoader.getInstance().disableServerRecipe(recipe)){
               getInventory().setItem(clickedSlot, null);
//               getRecipes().remove(recipe);
           }
       }
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
