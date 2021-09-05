package com.dutchjelly.craftenhance.crafthandling;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.RecipeType;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RecipeLoader implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(CraftEnhance.self().getConfig().getBoolean("learn-recipes"))
            Adapter.DiscoverRecipes(e.getPlayer(), getLoadedServerRecipes());
    }

    //Ensure one instance
    private static RecipeLoader instance = null;
    public static RecipeLoader getInstance(){
        return instance == null ? instance = new RecipeLoader(Bukkit.getServer()) : instance;
    }

    public static void clearInstance(){
        instance = null;
    }

    @Getter
    private List<Recipe> serverRecipes = new ArrayList<>();

    @Getter
    private List<Recipe> disabledServerRecipes = new ArrayList<>();

    private Map<String, Recipe> loaded = new HashMap<>();
    private Server server;

    @Getter
    private final Map<RecipeType, List<RecipeGroup>> mappedGroupedRecipes = new HashMap<>();

    @Getter
    private List<EnhancedRecipe> loadedRecipes = new ArrayList<>();

    private RecipeLoader(Server server){
        this.server = server;

        server.recipeIterator().forEachRemaining(serverRecipes::add);

        for (RecipeType type : RecipeType.values()) {
            mappedGroupedRecipes.put(type, new ArrayList<>());
        }
    }

    //Adds or merges group with existing group.
    private RecipeGroup addGroup(RecipeGroup newGroup, RecipeType type){

        if(newGroup == null) return null;

        List<RecipeGroup> groupedRecipes = mappedGroupedRecipes.get(type);
        for(RecipeGroup group : groupedRecipes){
//            Debug.Send("Looking if two enhanced recipes are similar for merge.");
            if(newGroup.getEnhancedRecipes().stream().anyMatch(x -> group.getEnhancedRecipes().stream().anyMatch(x::isSimilar))){
                return group.mergeWith(newGroup);
            }
        }

        groupedRecipes.add(newGroup);
        return newGroup;
    }

    public RecipeGroup findGroup(EnhancedRecipe recipe){
        return mappedGroupedRecipes.get(recipe.getType()).stream().filter(x -> x.getEnhancedRecipes().contains(recipe)).findFirst().orElse(null);
    }

    //Find groups that contain at least one recipe that maps to result.
    public List<RecipeGroup> findGroupsByResult(ItemStack result, RecipeType type){
        List<RecipeGroup> originGroups = new ArrayList<>();
        for(RecipeGroup group : mappedGroupedRecipes.get(type)){
            if(group.getEnhancedRecipes().stream().anyMatch(x -> result.equals(x.getResult())))
                originGroups.add(group);
            else if(group.getServerRecipes().stream().anyMatch(x -> result.equals(x.getResult())))
                originGroups.add(group);
        }
        return originGroups;
    }

    public RecipeGroup findMatchingGroup(ItemStack[] matrix, RecipeType type){
        for(RecipeGroup group : mappedGroupedRecipes.get(type)){
            if(group.getEnhancedRecipes().stream().anyMatch(x -> x.matches(matrix)))
                return group;
        }
        return null;
    }

    public RecipeGroup findSimilarGroup(EnhancedRecipe recipe){
        return mappedGroupedRecipes.get(recipe.getType()).stream().filter(x ->
                x.getEnhancedRecipes().stream().anyMatch(y -> y.isSimilar(recipe)) ||
                        x.getServerRecipes().stream().anyMatch(y -> recipe.isSimilar(y))
        ).findFirst().orElse(null);
    }

    //There can only be one group that matches a matrix, because that's how they're grouped
//    public List<RecipeGroup> findMatchingGroups(ItemStack[] matrix, RecipeType type){
//        List<RecipeGroup> matchingGroups = new ArrayList<>();
//        for(RecipeGroup group : mappedGroupedRecipes.get(type)){
//            try{
//                if(group.getEnhancedRecipes().stream().anyMatch(x -> x.matches(matrix)))
//                    matchingGroups.add(group);
//            } catch(ClassCastException e){}
//        }
//        return matchingGroups;
//    }

    public boolean isLoadedAsServerRecipe(EnhancedRecipe recipe){
        return loaded.containsKey(recipe.getKey());
    }

    private void syncServerRecipeState(){
        server.clearRecipes();
        serverRecipes.forEach(server::addRecipe);
        loaded.values().forEach(server::addRecipe);
    }

    public void unloadAll(){
        mappedGroupedRecipes.clear();
        for (RecipeType type : RecipeType.values()) {
            mappedGroupedRecipes.put(type, new ArrayList<>());
        }
        serverRecipes.clear();
        loaded.clear();
        server.resetRecipes();
    }

    public void unloadRecipe(EnhancedRecipe recipe){
        RecipeGroup group = findGroup(recipe);
        loadedRecipes.remove(recipe);
        if(group == null) {
            printGroupsDebugInfo();
            Bukkit.getLogger().log(Level.SEVERE, "Could not unload recipe from groups because it doesn't exist.");
            return;
        }
        Recipe serverRecipe = loaded.get(recipe.getKey());

        //Only unload from server if there are no similar server recipes.
        if(serverRecipe != null){
            //We can't remove recipes with the iterator because it's immutable.
            loaded.remove(recipe.getKey());
            syncServerRecipeState();
        }

        /* TODO (Optimization) When e.g. a shapeless recipe disappears from a recipe, not all recipes in that group are
            similar anymore. So detect this and make sure they get split up. */

        //Remove entire recipe group if it's the last enhanced recipe, or remove a single recipe from the group.
        if(group.getEnhancedRecipes().size() == 1)
            mappedGroupedRecipes.get(recipe.getType()).remove(group);
        else group.getEnhancedRecipes().remove(recipe);
        Debug.Send("Unloaded a recipe");
        printGroupsDebugInfo();
    }

    public void loadRecipe(@NonNull EnhancedRecipe recipe){

        if(recipe.validate() != null) {
            Messenger.Error("There's an issue with recipe " + recipe.getKey() + ": " + recipe.validate());
            return;
        }

        if(loaded.containsKey(recipe.getKey()))
            unloadRecipe(recipe);

        List<Recipe> similarServerRecipes = new ArrayList<>();
        for(Recipe r : serverRecipes){
            if(recipe.isSimilar(r)){
                similarServerRecipes.add(r);
            }
        }
        Recipe alwaysSimilar = null;
        for(Recipe r : similarServerRecipes){
            if(recipe.isAlwaysSimilar(r)){
                alwaysSimilar = r;
                break;
            }
        }
        //Only load the recipe if there is not a server recipe that's always similar.
        if(alwaysSimilar == null){
            Recipe serverRecipe = recipe.getServerRecipe();
            server.addRecipe(serverRecipe);
            Debug.Send("Added server recipe for " + serverRecipe.getResult().toString());
            loaded.put(recipe.getKey(), serverRecipe);
            if(CraftEnhance.self().getConfig().getBoolean("learn-recipes"))
                Bukkit.getServer().getOnlinePlayers().forEach(x -> Adapter.DiscoverRecipes(x, Arrays.asList(serverRecipe)));
        }else{
            Debug.Send("Didn't add server recipe for " + recipe.getKey() + " because a similar one was already loaded: " + alwaysSimilar.toString() + " with the result " + alwaysSimilar.getResult().toString());
        }

        RecipeGroup group = new RecipeGroup();
        group.setEnhancedRecipes(Arrays.asList(recipe));
        group.setServerRecipes(similarServerRecipes);
        addGroup(group, recipe.getType());
        loadedRecipes.add(recipe);
    }

    public List<Recipe> getLoadedServerRecipes(){
        return new ArrayList<>(loaded.values());
    }

    public void printGroupsDebugInfo(){
        for (Map.Entry<RecipeType, List<RecipeGroup>> recipeGrouping : mappedGroupedRecipes.entrySet()) {
            Debug.Send("Groups for recipes of type: " + recipeGrouping.getKey().toString());
            for (RecipeGroup group : recipeGrouping.getValue()) {
                Debug.Send("  <group>");
                Debug.Send("     enhanced recipes: " + group.getEnhancedRecipes().stream().filter(x -> x != null).map(x -> x.getResult().toString()).collect(Collectors.joining(", ")));
                Debug.Send("     server recipes: " + group.getServerRecipes().stream().filter(x -> x != null).map(x -> x.getResult().toString()).collect(Collectors.joining(", ")));
            }
        }
    }

    public boolean disableServerRecipe(Recipe r){
        if(serverRecipes.contains(r)) {
            Debug.Send("[Recipe Loader] disabling server recipe for " + r.getResult().getType().name());

            serverRecipes.remove(r);
            disabledServerRecipes.add(r);

            RecipeType type = RecipeType.getType(r);
            if(type != null) {
                for (RecipeGroup recipeGroup : mappedGroupedRecipes.get(type)) {
                    if(recipeGroup.getServerRecipes().contains(r)) {
                        recipeGroup.getServerRecipes().remove(r);
                        EnhancedRecipe alwaysSimilar = recipeGroup.getEnhancedRecipes().stream().filter(x -> x.isAlwaysSimilar(r)).findFirst().orElse(null);
                        //If there's a recipe that's always similar, we have to load it again.
                        if(alwaysSimilar != null)
                            loaded.put(alwaysSimilar.getKey(), alwaysSimilar.getServerRecipe());
                    }
                }
            }
            syncServerRecipeState();
            return true;
        }
        return false;
    }

    public boolean enableServerRecipe(Recipe r){
        if(!serverRecipes.contains(r)) {
            Debug.Send("[Recipe Loader] enabling server recipe for " + r.getResult().getType().name());
            serverRecipes.add(r);
            disabledServerRecipes.remove(r);

            RecipeType type = RecipeType.getType(r);
            if(type != null) {
                for (RecipeGroup recipeGroup : mappedGroupedRecipes.get(type)) {
                    if(recipeGroup.getEnhancedRecipes().stream().anyMatch(x -> x.isSimilar(r))) {
                        recipeGroup.getServerRecipes().add(r);
                        EnhancedRecipe alwaysSimilar = recipeGroup.getEnhancedRecipes().stream().filter(x -> x.isAlwaysSimilar(r)).findFirst().orElse(null);

                        //If there's a recipe that's always similar, we have to unload it again.
                        if(alwaysSimilar != null)
                            loaded.remove(alwaysSimilar.getKey());
                    }
                }
            }
            syncServerRecipeState();
            return true;
        }
        return false;
    }

    public void disableServerRecipes(List<Recipe> disabledServerRecipes){
        //No need to be efficient here, this'll only run once.
        disabledServerRecipes.forEach(x -> disableServerRecipe(x));
    }
}
