package me.cvenomz.OwnBlocks;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OwnBlocksPlayerListener extends PlayerListener{
	
	OwnBlocks pluginRef;
	private Map<OBBlock, String> database;
	
	public OwnBlocksPlayerListener(OwnBlocks ob)
	{
		pluginRef = ob;
		database = pluginRef.database;
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		// TODO Auto-generated method stub
		//super.onPlayerJoin(event);
		if (pluginRef.getEnabledOnLogin())
		{
			String player = event.getPlayer().getName();
			pluginRef.addPlayer(player);
		}
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		String player = event.getPlayer().getName();
		pluginRef.removePlayer(player);
	}
	
	/*@Override
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		pluginRef.debugMessage("PlayerInteractEvent");
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if (pluginRef.hasPermission(e.getPlayer(), "OwnBlocks.info"))
			if (e.getItem() != null && e.getItem().getTypeId() == pluginRef.getInfoID())
				handleInfo(e);
	}*/
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		pluginRef.debugMessage("PlayerInteractEvent");
		if (e.getItem() == null)
			return;
		
		//Info
		if (e.getItem().getTypeId() == pluginRef.getInfoID())
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
				if (pluginRef.hasPermission(e.getPlayer(), "OwnBlocks.info"))
					handleInfo(e);
		
		//Add
		if (e.getItem().getTypeId() == pluginRef.getAddID())
			if (e.getAction() == Action.LEFT_CLICK_BLOCK)
				if (pluginRef.hasPermission(e.getPlayer(), "OwnBlocks.add"))
					handleAdd(e);
		
	}
	
	private void handleInfo(PlayerInteractEvent e)
	{
		OBBlock obb = new OBBlock(e.getClickedBlock());
		if (database.containsKey(obb))
		{
			e.getPlayer().sendMessage(ChatColor.YELLOW + "Block owned by: " + database.get(obb));
		}
		else
		{
			e.getPlayer().sendMessage(ChatColor.YELLOW + "Block not owned");
		}
	}
	
	private void handleAdd(PlayerInteractEvent e)
	{
		OBBlock obb = new OBBlock(e.getClickedBlock());
		if (database.containsKey(obb))
		{
			e.getPlayer().sendMessage(ChatColor.YELLOW + "Can't add " + obb.toString() + " already owned");
		}
		else
		{
			database.put(obb, e.getPlayer().getName());
			e.getPlayer().sendMessage(ChatColor.YELLOW + "Added " + obb.toString());
		}
	}

}
