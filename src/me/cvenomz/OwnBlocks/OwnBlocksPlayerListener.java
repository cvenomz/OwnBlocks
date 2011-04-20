package me.cvenomz.OwnBlocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

public class OwnBlocksPlayerListener extends PlayerListener {
	
	OwnBlocks pluginRef;
	Map<Player, ArrayList> database;
	Map<Location, Player> interactPending;
	
	
	public OwnBlocksPlayerListener(OwnBlocks ob)
	{
		pluginRef = ob;
		database = pluginRef.database;
		interactPending = pluginRef.pending;
	}

	public void onPlayerInteract(PlayerInteractEvent e)
	{
		processPending();
		if (!database.containsKey(e.getPlayer()))
			database.put(e.getPlayer(), new ArrayList());
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
			handleRightClickBlock(e);
		
			
	}
	
	private void handleRightClickBlock(PlayerInteractEvent e)
	{
		Player player = e.getPlayer();
		Location loc = e.getClickedBlock().getRelative(e.getBlockFace()).getLocation();
		interactPending.put(loc, player);
	}
	
	public void processPending()
	{
		if (interactPending.isEmpty())
			return;
		Iterator<Location> locs = (Iterator<Location>) interactPending.keySet();
		Location loc;
		while (locs.hasNext())
		{
			loc = locs.next();
			if (loc.getBlock().getTypeId() != 00) //if block is not air
				database.get(pending.get(loc)).add(loc.getBlock());
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
					return player;
			}
		}
		
		return null;
	}

}
