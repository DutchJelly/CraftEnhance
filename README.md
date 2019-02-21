# CraftEnhance

This is the repository of CraftEnhance. 

The basic idea of the plugin is that it allows users to make custom recipes on minecraft servers that run on bukkit/spigot. One thing that this plugin is unique in is that it allows items in the recipe to have metadata like enchantments or custom names.

The plugin is properly documented here for users: https://dev.bukkit.org/projects/craftenhance.

If you want to use this plugin as an API, you need to get the instance of the main JavaPlugin extended class. This contains all objects that get used. For now, as I don't have a proper documentation on the API part, you can just look at the source code to see what everything does.

Some examples of why I would use this plugin as an API in certain cases is to:
 - Access the GUI system.
 - Use the messaging system.
 - Use the command handler.
 - Create own recipes programatically.
 - Use the debugger/messenger.
 - Add own GUI's by extending the GUIElement interface class.
 
 
