# CraftEnhance

This is the repository of CraftEnhance. 

The basic idea of the plugin is that it allows users to make custom recipes on minecraft servers that run on the bukkit/spigot API. One thing that this plugin is unique in is that it allows items in recipes to have metadata like enchantments or custom names. It also features full GUI support, which makes in-game recipe editing/creation possible.

If you want to use this plugin as an API, you need to get the instance of the main JavaPlugin extended class. This contains all objects that get used. For now, as I don't have a proper documentation on the API part, you can just look at the source code to see what everything does. There's a proper api in the works called CraftEnhanceAPI.

Some examples of why I would use this plugin as an API in certain cases is to:
 - Access the GUI system.
 - Use the messaging system.
 - Use the command handler.
 - Create own recipes programatically.
 - Use the debugger/messenger.
 - Add own GUI's by extending the GUIElement interface class.

### Changelog

##### 2.4.2
```diff
+ Now `ExactChoice` is only used when `learn-recipes` is set to true.
+ [BUGFIX] `NameSpacedKey` is now using the correct Constructor parameter types in the reflection.
+ [BUGFIX] Newer items don't have ItemData, so the plugin now falls back on Materials to load recipes.
+ Added error handling for when recipes aren't loaded properly. It'll now get skipped instead of throwing errors.
```

##### 2.4.2-beta
```diff
+ Added server recipe disabler gui `/ceh disable`.
+ Added proper 2*2 crafting support.
+ Only one build of the plugin will now support the game versions 1.9-1.16, seperate builds with slightly different configurations were needed before.
+ [BUGFIX] matchmeta/hidden property of WBRecipe is now serialized properly.
+ [BUGFIX] Players now only discover recipes when it's actually enabled in the config.
```
##### 2.4.1
```diff
+ Custom head support in GUI's
+ Option to open page in the recipes viewer with /recipes [number]
+ Option to set names for each page in the recipes viewer by adding `names:` list in the guiconfig file
+ Learning recipes is now configurable, listed as `learn-recipes: false` in the config by default
+ Option to make recipe hidden through the editor. 
+ Players that have edit perms or a ".hidden" suffix like: <recipe perm>.hidden they can see the hidden recipe.
+ Recipe permissions are now editable through the gui.
+ [BUGFIX] empty permissions did not work on recipes
+ [BUGFIX] dots in item names now don't break the items.yml file format
- Removed feature where the plugin disables when the wrong version is installed. Instead it shows some warnings now. 
```

##### 2.4.0
```diff
+ Fixed a bug where recipe editor would get opened instead of recipe viewer.
+ Allowed users to set empty permissions in "perms.recipe-viewer" in config.yml to specify that viewing requires no permissions.
+ Improved error handling on recipes with an empty result.
+ You can now also right click instead of middle click to open the editor of a clicked recipe.
```

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

