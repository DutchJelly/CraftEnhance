package com.dutchjelly.updatechecking;

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

        GithubLoader loader = GithubLoader.init(this);
        loader.readVersion();
        String version = loader.getVersion();
        if(version == null) return;
        version = version.trim();
        if(!isOutDated(version)) return;
        plugin.getMessenger().message(
                "There's a new version (" + version + ") of the plugin available.\n" +
                "The version can be downloaded here https://dev.bukkit.org/projects/craftenhance/files."
        );
    }

    public CraftEnhance getPlugin() {
        return plugin;
    }

    private boolean isOutDated(String version){
        String currentVersion = plugin.getDescription().getVersion();
        return currentVersion != version;
    }
}
