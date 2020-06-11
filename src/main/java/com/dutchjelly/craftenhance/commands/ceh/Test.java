package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.crafthandling.util.ItemMatchers;
import com.dutchjelly.craftenhance.crafthandling.util.ServerRecipeTranslator;
import com.dutchjelly.craftenhance.crafthandling.util.WBRecipeComparer;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@CommandRoute(cmdPath="ceh.test", perms="ceh.debug")
public class Test implements ICommand{
    @Override
    public String getDescription() {
        return "Unit tests for the plugin.";
    }

    @Override
    public void handlePlayerCommand(Player p, String[] args) {
//        if(args.length != 1)
//            p.sendMessage("Please specify a unit test index.");
//        unitTests(Integer.parseInt(args[0]), p);
    }

    @Override
    public void handleConsoleCommand(CommandSender sender, String[] args) {
        if(args.length != 1)
            sender.sendMessage("Please specify a unit test index.");
        try{
            unitTests(Integer.parseInt(args[0]), sender);
        }catch(NumberFormatException e) {
            sender.sendMessage("Use a number as input!!!!");
        }
    }


    private void unitTests(int index, CommandSender p){

        switch(index){
            case 1:
                testShapedComparer(p);
                break;
            case 2:
                testServerRecipeTranslator(p);
            default:
                p.sendMessage("no test specified");
                break;
        }
    }

    private ItemStack[] buildRecipe(String string){
        ItemStack it = new ItemStack(Material.DIAMOND);
        ItemStack[] built = new ItemStack[9];
        for(int i = 0; i < string.length(); i++){
            built[i] = string.charAt(i) == '-' ? null : it;
        }
        return built;
    }

    //Inefficient algorithm for comparing shapes so we can test the efficient algorithm.
    private String shiftToLeftTop(String s){
        if(s.trim() == "") return s;
        if(s.length() != 9) return s;
        while(s.startsWith("---")) s = s.substring(3) + "---";
        while(s.length() > 0 && s.charAt(0) == '-' && s.charAt(3) == '-' && s.charAt(6) == '-') s = s.substring(1) + '-';
        return s;
    }

    private void testShapedComparer(CommandSender p){
        ItemStack i = new ItemStack(Material.DIAMOND);

        p.sendMessage("testing some cornercases");

        //Test shape compare first.
        ItemStack[] a = buildRecipe("--i--i--i");
        ItemStack[] b = buildRecipe("i--i--i--");
        ItemStack[] c = buildRecipe("-i--i--i-");

        assert(WBRecipeComparer.shapeMatches(a, b, ItemMatchers::matchType));
        assert(WBRecipeComparer.shapeMatches(a, c, ItemMatchers::matchType));
        assert(WBRecipeComparer.shapeMatches(b, c, ItemMatchers::matchType));

        a = buildRecipe("----i-i--");
        b = buildRecipe("--i-i----");
        c = buildRecipe("-i-i-----");

        assert(WBRecipeComparer.shapeMatches(a, b, ItemMatchers::matchType));
        assert(WBRecipeComparer.shapeMatches(a, c, ItemMatchers::matchType));
        assert(WBRecipeComparer.shapeMatches(b, c, ItemMatchers::matchType));

        a = buildRecipe("--ii-----");
        b = buildRecipe("-----ii--");
        c = buildRecipe("---i-i---");
        assert(WBRecipeComparer.shapeMatches(a, b, ItemMatchers::matchType));
        assert(!WBRecipeComparer.shapeMatches(a, c, ItemMatchers::matchType));
        assert(!WBRecipeComparer.shapeMatches(b, c, ItemMatchers::matchType));

        /* Random tests */
        p.sendMessage("testing randomly generated recipes");
        final int randomtestcount = 50;
        Random r = new Random();
        for(int t = 0; t < randomtestcount; t++){
            String shape = "";
            int counter = 0; //count amount of items randomly generated
            for(int j = 0; j < 9; j++){
                shape += r.nextBoolean() ? '-' : 'i';
                if(shape.endsWith("i")){
                    counter++;
                }
            }
            if(counter == 0) continue;
            a = buildRecipe(shape);
            b = buildRecipe(shiftToLeftTop(shape));
            assert(WBRecipeComparer.shapeMatches(a, b, ItemMatchers::matchType));

            //Try to shuffle elements. and check if they match with randomly generated one
            for(int matchtest = 0; matchtest < 200; matchtest++){
                List<String> shuffeledShape = Arrays.asList(shape.split(""));
                Collections.shuffle(shuffeledShape, r);
                String s = String.join("", shuffeledShape);
                boolean matches = shiftToLeftTop(s).equals(shiftToLeftTop(shape));
                c = buildRecipe(s);
                assert((matches && WBRecipeComparer.shapeMatches(a,c, ItemMatchers::matchType))
                || (!matches && !WBRecipeComparer.shapeMatches(a,c, ItemMatchers::matchType)));
            }
        }


        /* test meta matching */
        p.sendMessage("testing for special item meta and item quantity");
        //Other types of itemmeta
        ItemStack item = new ItemStack(Material.SKULL);
        SkullMeta sMeta = (SkullMeta)item.getItemMeta();
        sMeta.setOwner("DutchJelly");
        item.setItemMeta(sMeta);
        ItemStack item2 = new ItemStack(Material.SKULL);
        SkullMeta sMeta2 = (SkullMeta)item2.getItemMeta();
        sMeta2.setOwner("GamerGamer1000");
        item2.setItemMeta(sMeta2);

        Arrays.fill(a, item);
        Arrays.fill(b, item2);
        Arrays.fill(c, item);

        assert(!WBRecipeComparer.shapeMatches(a,b, ItemMatchers::matchMeta));
        assert(WBRecipeComparer.shapeMatches(a,b, ItemMatchers::matchType));
        assert(WBRecipeComparer.shapeMatches(a,c, ItemMatchers::matchMeta));

        //quantity shouldn't be matched
        item2.setAmount(2);

        assert(WBRecipeComparer.shapeMatches(a,b, ItemMatchers::matchType)
                && WBRecipeComparer.shapeMatches(a,b, ItemMatchers::matchMeta));

        p.sendMessage("All tests executed!");
    }


    private void testServerRecipeTranslator(CommandSender p){


        final int testAmount = 10;
        ItemStack[] items = new ItemStack[]{new ItemStack(Material.DIAMOND), new ItemStack(Material.LADDER), new ItemStack(Material.STICK)};
        Random r = new Random();
        p.sendMessage("testing randomly generated shaped recipes translations");
        for (int i = 0; i < testAmount; i++) {
            ItemStack[] recipe = new ItemStack[9];
            for(int j = 0; j < 9; j++){
                if(r.nextBoolean()){
                    recipe[i] = items[r.nextInt(3)];
                }else recipe[i] = null;
            }
            ShapedRecipe sr = ServerRecipeTranslator.translateShapedEnhancedRecipe(recipe, items[0]);
            ItemStack[] original = ServerRecipeTranslator.translateShapedRecipe(sr);
            assert(WBRecipeComparer.shapeMatches(original, recipe, ItemMatchers::matchType));
        }



    }
}
