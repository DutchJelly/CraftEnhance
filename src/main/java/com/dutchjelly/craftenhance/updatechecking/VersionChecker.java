package com.dutchjelly.craftenhance.updatechecking;

import com.dutchjelly.craftenhance.CraftEnhance;

public class VersionChecker {

    private CraftEnhance plugin;

    public static VersionChecker init(CraftEnhance plugin){
        VersionChecker checker = new VersionChecker();
        checker.plugin = plugin;
        return checker;
    }

    public void runVersionCheck(){
        if(!plugin.getConfig().getBoolean("enable-updatechecker")) return;

        plugin.getMessenger().message("Checking for newer versions...");

        GithubLoader loader = GithubLoader.init(this);
        loader.readVersion();
        String version = loader.getVersion();
        if(version == null) return;
        version = version.trim();
        if(!isOutDated(version)){
            plugin.getMessenger().message("CraftEnhance is up to date.");
        } else{
            plugin.getMessenger().message("There's a new version (" + version + ") of the plugin available.");
            plugin.getMessenger().message("This version can be downloaded at https://dev.bukkit.org/projects/craftenhance/files.");
        }
    }

    public CraftEnhance getPlugin() {
        return plugin;
    }

    private boolean isOutDated(String version){
        String currentVersion = plugin.getDescription().getVersion();
        return !version.equals(currentVersion);
    }
}
