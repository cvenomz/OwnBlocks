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
	private Map<Player, ArrayList> database;
	Logger log;
	
	OwnBlocksBlockListener(OwnBlocks ob)
	{
		pluginRef = ob;
		database = pluginRef.database;
		log = pluginRef.log;
	}
	
	public void onBlockBreak(BlockBreakEvent e)
	{
		pluginRef.log.info("Break from " + e.getPlayer().getName());
		if (whoOwns(e.getBlock().getLocation()) == null || whoOwns(e.getBlock().getLocation()).equals(e.getPlayer()))
				return;
		else
		{
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.DARK_RED + "That block is not yours to break");
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent e)
	{
		pluginRef.log.info("received Place event from " + e.getPlayer().getName());
		if (!database.containsKey(e.getPlayer()))
			database.put(e.getPlayer(), new ArrayList());
		
		if (whoOwns(e.getBlock().getLocation()) == null)
			database.get(e.getPlayer()).add(e.getBlockPlaced());
		else if (!whoOwns(e.getBlock().getLocation()).equals(e.getPlayer()))
		{
			database.get(whoOwns(e.getBlock().getLocation())).remove(e.getBlock());
			database.get(e.getPlayer()).add(e.getBlock());
		}
	}
	
	private Player whoOwns(Location loc)
	{
		Iterator<Player> players = database.keySet().iterator();
		Iterator<Block> blocks;
		Player player;
		Block block;
		while (players.hasNext())
		{
			player = players.next();
			blocks = database.get(player).iterator();
			while (blocks.hasNext())
			{
				block = blocks.next();
				if (block.getLocation().equals(loc))
				{
					log.info("Block owned by " + player.getName());
					return player;
				}
			}
		}
		log.info("No one owns this block");
		return null;
	}

}
