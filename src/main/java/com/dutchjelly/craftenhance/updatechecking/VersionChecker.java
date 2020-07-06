package com.dutchjelly.craftenhance.updatechecking;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.messaging.Messenger;

import java.util.Arrays;

public class VersionChecker {

    private CraftEnhance plugin;

    public static VersionChecker init(CraftEnhance plugin){
        VersionChecker checker = new VersionChecker();
        checker.plugin = plugin;
        return checker;
    }

    public void runUpdateCheck(){
        if(!plugin.getConfig().getBoolean("enable-updatechecker")) return;

        GithubLoader loader = GithubLoader.init(this);
        loader.readVersion();
        String version = loader.getVersion();
        if(version == null) return;
        version = version.trim();
        if(!isOutDated(version)){
            Messenger.Message("CraftEnhance is up to date.");
        } else{
            Messenger.Message("There's a new version (" + version + ") of the plugin available on https://dev.bukkit.org/projects/craftenhance/files.");
        }
    }

    public boolean runVersionCheck(){
        String serverVersion = plugin.getServer().getBukkitVersion();
        Messenger.Message("Running a version check to check that the server is compatible with game version " + String.join(", ", Adapter.CompatibleVersions()) + ".");
        for(String version : Adapter.CompatibleVersions()){
            if(serverVersion.contains(version)){
                Messenger.Message("The correct version is installed.");
                return true;
            }
        }
        Messenger.Message("");
        Messenger.Message("!! Incompatibility found !!");
        Messenger.Message("The installed version of CraftEnhance only supports spigot/bukkit versions \"" + String.join(", ", Adapter.CompatibleVersions()) + "\"");
        Messenger.Message("while your server is running " + serverVersion + ".");
        Messenger.Message("The correct version can be installed here: https://dev.bukkit.org/projects/craftenhance/files");
        Messenger.Message("When installing the plugin make sure that the game version matches your bukkit or spigot version.");
        Messenger.Message("Please note that this incompatibility could cause duping glitches.");
        Messenger.Message("So because the incorrect plugin version is being used, the plugin has to be disabled.");
        return false;
    }


    public CraftEnhance getPlugin() {
        return plugin;
    }

    private boolean isOutDated(String version){
        String currentVersion = plugin.getDescription().getVersion();
        return !Arrays.stream(version.split("\n")).anyMatch(x -> x.equalsIgnoreCase(currentVersion));
    }
}
