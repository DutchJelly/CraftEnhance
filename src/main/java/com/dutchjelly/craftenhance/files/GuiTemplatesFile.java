package com.dutchjelly.craftenhance.files;

import com.dutchjelly.craftenhance.ConfigError;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GuiTemplatesFile {

    private YamlConfiguration templateConfig;
    private File file;

    private static final String GUI_FOLDER = "com.dutchjelly.craftenhance.gui.guis";
    private static final String GUI_FILE_NAME = "guitemplates.yml";

    private Map<Class<? extends GUIElement>, GuiTemplate> templates;
    private JavaPlugin plugin;

    public GuiTemplatesFile(JavaPlugin plugin){
        file = new File(plugin.getDataFolder(), GUI_FILE_NAME);
        this.plugin = plugin;
    }

    public void load(){
        if(!file.exists())
            plugin.saveResource(GUI_FILE_NAME, false);
        templateConfig = YamlConfiguration.loadConfiguration(file);
        templates = new HashMap<>();
        for(String key : templateConfig.getKeys(false)){
            Class<? extends GUIElement> clazz;
            try{
                clazz = (Class<? extends GUIElement>)Class.forName(GUI_FOLDER + "." + key);
            }catch(ClassNotFoundException e){
                Messenger.Error("Could not find class " + key);
                continue;
            }catch(ClassCastException e){
                Messenger.Error("Class " + key + " isn't a gui.");
                continue;
            }
            try{
                templates.put(clazz, new GuiTemplate(templateConfig.getConfigurationSection(key)));
            }catch(ConfigError configError){
                Messenger.Error("There was an error that occurred when loading the gui template of " + key + ":");
                configError.printStackTrace();
            }
        }
    }

    public GuiTemplate getTemplate(Class<? extends GUIElement> clazz){
        if(!templates.containsKey(clazz)){
            throw new ConfigError("Cannot find template of " + clazz.getSimpleName());
        }

        return templates.get(clazz);
    }
}
