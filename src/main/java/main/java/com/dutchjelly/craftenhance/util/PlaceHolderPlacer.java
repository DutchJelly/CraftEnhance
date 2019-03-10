package main.java.com.dutchjelly.craftenhance.util;

import org.bukkit.entity.Player;

public class PlaceHolderPlacer {
	
	//Replaces <player> with player.getName();
	public static String place(String msg, Player player){
		return msg.replace("<player>", player.getName());
	}
	
	//Maybe add more in the future.
}
