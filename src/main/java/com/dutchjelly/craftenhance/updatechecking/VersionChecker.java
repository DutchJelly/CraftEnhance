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

    public void runVersionCheck(){
        String serverVersion = plugin.getServer().getBukkitVersion();
        for(String version : Adapter.CompatibleVersions()){
            if(serverVersion.contains(version)){
                return;
            }
        }
        plugin.getMessenger().message("");
        plugin.getMessenger().message("!! Incompatibility found !!");
        plugin.getMessenger().message("Your installed CraftEnhance version is not compatible with your Bukkit or Spigot version \"" + serverVersion + "\".");
        plugin.getMessenger().message("The installed version of CraftEnhance only supports the following versions: " + String.join(", ", Adapter.CompatibleVersions()) + ".");
        plugin.getMessenger().message("The correct version can be installed here: https://dev.bukkit.org/projects/craftenhance/files");
        plugin.getMessenger().message("When installing, look at the \"Game Version\" column and pick one that matches your Spigot or Bukkit jar version that runs the server.");
        plugin.getMessenger().message("Please note that this incompatibility could cause duping glitches.");
        plugin.getMessenger().message("");
    }

    public CraftEnhance getPlugin() {
        return plugin;
    }

    private boolean isOutDated(String version){
        String currentVersion = plugin.getDescription().getVersion();
        return !version.equals(currentVersion);
    }
}
