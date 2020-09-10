package com.dutchjelly.craftenhance.gui.templates;

import com.dutchjelly.craftenhance.ConfigError;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class GuiTemplate {

    private static final int RowSize = 9;

    @Getter
    private final ItemStack[] template;

    @Getter
    private final String invTitle;

    @Getter
    private final List<String> invTitles;

    @Getter @NonNull
    private final Map<Integer, ButtonType> buttonMapping;

    @Getter @NonNull
    private final List<Integer> fillSpace;

    public GuiTemplate(ConfigurationSection config){

        String name = config.getString("name");
        List<String> names = config.getStringList("names");

        if(name == null && names == null)
            throw new ConfigError("no gui name is specified");

        if(names == null) names = new ArrayList<>();

        if(name != null)
            names.add(name);
        names = names.stream().map(x -> ChatColor.translateAlternateColorCodes('&', x)).collect(Collectors.toList());

        if(names.isEmpty())
            throw new ConfigError("a template has no name");


        ConfigurationSection templateSection = config.getConfigurationSection("template");
        if(templateSection == null){
            throw new ConfigError("template is not specified");
        }
        List<ItemStack> templateInventoryContent = new ArrayList<>();
        for(String key : templateSection.getKeys(false)){
            List<Integer> slots = parseRange(key);
            final ItemStack item = new GuiItemTemplate(templateSection.getConfigurationSection(key)).getItem();
            for(int slot : slots){
                while(templateInventoryContent.size()-1 < slot){
                    //go in steps of 9 because inventories are always in rows of 9
                    templateInventoryContent.addAll(Arrays.asList(new ItemStack[RowSize]));
                }
                templateInventoryContent.set(slot, item.clone());
            }
        }
        invTitles = names;
        invTitle = names.get(0);
        template = templateInventoryContent.stream().toArray(ItemStack[]::new);

        ConfigurationSection buttonSection = config.getConfigurationSection("button-mapping");

        buttonMapping = new HashMap<>();
        if(buttonSection != null){
            for(String s : buttonSection.getKeys(false)){
                List<Integer> slots = parseRange(s);
                ButtonType btn = ButtonType.valueOf(buttonSection.getString(s));
                slots.forEach(x -> buttonMapping.put(x, btn));
            }
        }

        fillSpace = parseRange(config.getString("fill-space"));
    }

    private List<Integer> parseRange(String range){
        List<Integer> slots = new ArrayList<>();

        //Allow empty ranges.
        if(range == null || range == "") return slots;

        try{
            for(String subRange : range.split(",")){
                if(subRange == "") continue;
                if (subRange.contains("-")) {
                    int first = Integer.valueOf(subRange.split("-")[0]);
                    int second = Integer.valueOf(subRange.split("-")[1]);
                    slots.addAll(IntStream.range(first, second + 1).mapToObj(x -> x).collect(Collectors.toList()));
                } else slots.add(Integer.valueOf(subRange));
            }
        }catch(NumberFormatException e){
            throw new ConfigError("Couldn't parse range " + range);
        }
        return slots;
    }

}
