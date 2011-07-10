package me.cvenomz.OwnBlocks;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class MysqlPlayerListener extends PlayerListener{
    
    OwnBlocks pluginRef;
    ConfigManager config;
    //private Map<OBBlock, String> database;
    
    public MysqlPlayerListener(OwnBlocks ob, ConfigManager conf)
    {
        pluginRef = ob;
        config = conf;
        //database = pluginRef.database;
    }
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        // TODO Auto-generated method stub
        //super.onPlayerJoin(event);
        if (config.getEnabledOnLogin())
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
        if (e.getItem().getTypeId() == config.getInfoID())
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
                if (pluginRef.hasPermission(e.getPlayer(), "OwnBlocksX.info"))
                    handleInfo(e);
        
        //Add
        if (e.getItem().getTypeId() == config.getAddID())
            if (e.getAction() == Action.LEFT_CLICK_BLOCK)
                if (pluginRef.hasPermission(e.getPlayer(), "OwnBlocksX.add"))
                    handleAdd(e);
        
    }
    
    private void handleInfo(PlayerInteractEvent e)
    {
        Block b = e.getClickedBlock();
        MysqlBlock eventBlock = new MysqlBlock(b, e.getPlayer().getName(), null, null);
        MysqlBlock databaseBlock = pluginRef.getMysqlDatabase().getBlock(eventBlock);
        
        if (databaseBlock != null) //and therefore IS in the database
        {
            e.getPlayer().sendMessage(ChatColor.YELLOW + "Block owned by: " + databaseBlock.getOwner());
        }
        else
        {
            e.getPlayer().sendMessage(ChatColor.YELLOW + "Block not owned");
        }
    }
    
    private void handleAdd(PlayerInteractEvent e)
    {
        Block b = e.getClickedBlock();
        MysqlBlock eventBlock = new MysqlBlock(b, e.getPlayer().getName(), null, null);
        MysqlBlock databaseBlock = pluginRef.getMysqlDatabase().getBlock(eventBlock);
        
        if (databaseBlock != null) //and therefore IS in the database
        {
            e.getPlayer().sendMessage(ChatColor.YELLOW + "Can't add this block; it is already owned");
        }
        else
        {
            pluginRef.getMysqlDatabase().addBlock(eventBlock);
            e.getPlayer().sendMessage(ChatColor.YELLOW + "Added " + eventBlock);
        }
    }

}
