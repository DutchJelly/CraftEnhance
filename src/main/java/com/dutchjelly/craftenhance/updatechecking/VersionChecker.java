package com.dutchjelly.craftenhance.updatechecking;

import com.dutchjelly.bukkitadapter.Adapter;
import com.dutchjelly.craftenhance.CraftEnhance;

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
            plugin.getMessenger().message("CraftEnhance is up to date.");
        } else{
            plugin.getMessenger().message("There's a new version (" + version + ") of the plugin available on https://dev.bukkit.org/projects/craftenhance/files.");
        }
    }

    public boolean runVersionCheck(){
        String serverVersion = plugin.getServer().getBukkitVersion();
        plugin.getMessenger().message("Running a version check to check that the server is compatible with game version " + String.join(", ", Adapter.CompatibleVersions()) + ".");
        for(String version : Adapter.CompatibleVersions()){
            if(serverVersion.contains(version)){
                plugin.getMessenger().message("The correct version is installed.");
                return true;
            }
        }
        plugin.getMessenger().message("");
        plugin.getMessenger().message("!! Incompatibility found !!");
        plugin.getMessenger().message("The installed version of CraftEnhance only supports spigot/bukkit versions \"" + String.join(", ", Adapter.CompatibleVersions()) + "\"");
        plugin.getMessenger().message("while your server is running " + serverVersion + ".");
        plugin.getMessenger().message("The correct version can be installed here: https://dev.bukkit.org/projects/craftenhance/files");
        plugin.getMessenger().message("When installing the plugin make sure that the game version matches your bukkit or spigot version.");
        plugin.getMessenger().message("Please note that this incompatibility could cause duping glitches.");
        plugin.getMessenger().message("So because the incorrect plugin version is being used, the plugin has to be disabled.");
        return false;
    }


    public CraftEnhance getPlugin() {
        return plugin;
    }

    private boolean isOutDated(String version){
        String currentVersion = plugin.getDescription().getVersion();
        return !version.equals(currentVersion);
    }
}
