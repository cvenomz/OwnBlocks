package me.cvenomz.OwnBlocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class OwnBlocksBlockListener extends BlockListener{
	
	private OwnBlocks pluginRef;
	//private Map<Location, Player> blockPending;
	private Map<OBBlock, String> database;
	Logger log;
	
	OwnBlocksBlockListener(OwnBlocks ob)
	{
		pluginRef = ob;
		database = pluginRef.database;
		log = pluginRef.log;
	}
	
	public void onBlockBreak(BlockBreakEvent e)
	{	
		Block b = e.getBlock();
		OBBlock obb = new OBBlock(b);
		String player = e.getPlayer().getName();
		//listAll();
		//e.getPlayer().sendMessage(ChatColor.DARK_BLUE + "Break");
		if (database.containsKey(obb))
		{
			//e.getPlayer().sendMessage(ChatColor.DARK_RED + "true");
			if (!database.get(obb).equals(player))
			{
				//e.getPlayer().sendMessage(ChatColor.AQUA + "true");
				e.setCancelled(true);
			}
			else
				database.remove(obb);
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent e)
	{
		//Check if player is in the 'active' arraylist
		if (pluginRef.activatedPlayers.contains(e.getPlayer().getName()))
		{
			//Check if block ID is excluded
			//log.info(e.getPlayer().getName() + " placing " + e.getBlockPlaced().getTypeId());
			if (!pluginRef.exclude.contains(e.getBlockPlaced().getTypeId()))
			{
				//log.info("Stored");
				OBBlock obb = new OBBlock(e.getBlockPlaced());
				database.put(obb, e.getPlayer().getName());
			}
		}
	}
	
	
	private void listAll()
	{
		log.info(database.keySet().toString());
	}

}
