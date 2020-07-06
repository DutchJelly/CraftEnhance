# CraftEnhance

This is the repository of CraftEnhance. 

The basic idea of the plugin is that it allows users to make custom recipes on minecraft servers that run on the bukkit/spigot API. One thing that this plugin is unique in is that it allows items in recipes to have metadata like enchantments or custom names. It also features full GUI support, which makes in-game recipe editing/creation possible.
 
These are some of the TODO's including future features. Any help in doing those will be appreciated.
 - [ ] Add categories
 - [ ] Option to hide or unlock recipes 


### Changelog

##### 2.4.0-Beta
```diff
+ Ability to customize gui's
+ Ability to customize position of recipe in the viewer
+ More efficient reworked crafting checking
+ API added to hook into crafting events
+ Shaped/shapeless crafting support
+ Meta/Type crafting support
+ Much better error-handling for invalid states in the plugin
+ Using Lombok for getters and setters
- Removed ordereditor because it was clunky and not modulair
```

##### 2.3.4
```diff
+ Plugin now disables when an incompatible version is used.
```
##### 2.3.3
```diff
+ Now supports crafting with dye's in versions below 1.13.
+ Checks for mirrored recipes, think of bows that you can craft in two ways.
+ Added version checker so people don't use the wrong version.
+ Now shows an info tag in the recipe editor with the permission, key and default result.
+ [BUGFIX] Made items actually save even when they are similar to other items.
+ [BUGFIX] Made sure that the command /ceh setpermission actually saves the permission.
```


### API
Listening to custom recipes being injected:
```java
private void example(){
  //register the listener that should implement CustomCraftListener
  CraftEnhanceAPI.registerListener(this::handleCraft);
}

public boolean handleCraft(IEnhancedRecipe r, Player p, Inventory inv, RecipeGroup alternatives){
  //You can basically do anything with this! This function will get called when a recipe matches and the user
  //has permissions for it. The alternatives are recipes that could also match, but are not yet checked or 
  //don't match completely.
  
  //return true to cancel the event
  return false;
}
```

Loading a recipe:
```java
private void loadRecipe(IEnhancedRecipe r){
   RecipeLoader.getInstance().loadRecipe(r);
}
```

There are countless options to hook into the plugin. I'm not going to document them all. Just open an issue if you want something to be documented.

