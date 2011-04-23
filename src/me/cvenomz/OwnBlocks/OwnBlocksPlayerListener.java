package me.cvenomz.OwnBlocks;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OwnBlocksPlayerListener extends PlayerListener{
	
	OwnBlocks pluginRef;
	
	public OwnBlocksPlayerListener(OwnBlocks ob)
	{
		pluginRef = ob;
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		// TODO Auto-generated method stub
		//super.onPlayerJoin(event);
		
		String player = event.getPlayer().getName();
		pluginRef.addPlayer(player);
	}
	
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		String player = event.getPlayer().getName();
		pluginRef.removePlayer(player);
	}

}
