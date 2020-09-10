package com.dutchjelly.craftenhance.gui.templates;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.ConfigError;
import com.dutchjelly.craftenhance.gui.util.SkullCreator;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GuiItemTemplate {

    public final static String GUI_SKULL_MATERIAL_NAME = "GUI_SKULL_ITEM";


    @Getter
    private final ItemStack item;

    public GuiItemTemplate(ConfigurationSection config){
        if(config == null){
            item = null;
            return;
        }

        String material = config.getString("material");
        if(material == null) throw new ConfigError("found null material");

        String color = config.getString("color");

        //This is so the same config can be used across versions. If the item is colored, it's type should be named
        //after the base type name, so e.g. STAINED_GLASS_PANE
        if(color == null){
            Material mat = Adapter.getMaterial(material);
            if(mat == null) throw new ConfigError("material " + material + " not found");
            item = new ItemStack(mat);
        }else{

            //Tricky way to support skull meta's using the color attribute as data.
            if(material.equalsIgnoreCase(GUI_SKULL_MATERIAL_NAME)){
                if(color.startsWith("uuid"))
                    item = SkullCreator.itemFromUuid(UUID.fromString(color.replaceFirst("uuid", "")));
                else if(color.startsWith("base64"))
                    item = SkullCreator.itemFromBase64(color.replaceFirst("base64", ""));
                else if(color.startsWith("url"))
                    item = SkullCreator.itemFromUrl(color.replaceFirst("url", ""));
                else throw new ConfigError("specified skull meta is invalid");
            }

            else{
                DyeColor dColor = DyeColor.valueOf(color);
                if(dColor == null) throw new ConfigError("color " + color + " not found");
                item = Adapter.getColoredItem(material, dColor);
            }
        }



        List<String> lore = config.getStringList("lore");
        if(lore != null)
            lore = lore.stream().map(x -> ChatColor.translateAlternateColorCodes('&', x)).collect(Collectors.toList());

        String name = ChatColor.translateAlternateColorCodes('&', config.getString("name"));

        boolean glow = config.getBoolean("glow");

        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore == null ? new ArrayList<>() : lore); //avoid null lore

        meta.setDisplayName(name);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        if(glow)
            meta.addEnchant(Enchantment.DURABILITY, 10, true);

        item.setItemMeta(meta);
    }

}
