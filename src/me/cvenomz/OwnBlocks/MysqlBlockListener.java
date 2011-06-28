package me.cvenomz.OwnBlocks;

import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class MysqlBlockListener extends BlockListener{
    
    private OwnBlocks pluginRef;
    Logger log;
    boolean debug;
    
    MysqlBlockListener(OwnBlocks ob)
    {
        pluginRef = ob;
        //database = pluginRef.database;
        log = pluginRef.log;
        debug = pluginRef.debug;
    }
    
    public void onBlockBreak(BlockBreakEvent e)
    {   
        Block b = e.getBlock();
        String player = e.getPlayer().getName();
        MysqlBlock eventBlock = new MysqlBlock(b, player, null, null);
        MysqlBlock databaseBlock = pluginRef.getMysqlDatabase().getBlock(eventBlock);
        
        //Is block protected
        if (databaseBlock != null)
        {
            //Is player NOT the owner of the block?
            if (!databaseBlock.getOwner().equals(player))
            {
                if (pluginRef.hasPermission(e.getPlayer(), "OwnBlocks.ignoreOwnership")) //Is player a mod/OP
                    pluginRef.getMysqlDatabase().deleteBlock(databaseBlock);       //break block
                else                            //Player is not a mod/OP
                    e.setCancelled(true);       //dont break block, because they are not an OP, nor are they the owner.
            }
            else                                //Player is owner of block
                pluginRef.getMysqlDatabase().deleteBlock(databaseBlock);           //break block, because they are the owner
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
                
                //check iConomy
                if (pluginRef.useiConomy())
                {
                    debugMessage("Use iConomy == true");
                    Account account = pluginRef.iConomy.getBank().getAccount(e.getPlayer().getName());
                    if (account.getBalance() >= pluginRef.getRate())
                    {
                        account.subtract(pluginRef.getRate());
                        String player = e.getPlayer().getName();
                        MysqlBlock mb = new MysqlBlock(e.getBlockPlaced(), player, null, null);
                        pluginRef.getMysqlDatabase().addBlock(mb);
                        debugMessage("acct. balance >= Rate, iConomy block placed");
                    }
                    else
                    {
                        debugMessage("acct. funds insufficient, block not placed");
                        e.getPlayer().sendMessage(ChatColor.YELLOW + "You dont have enough money to place this block it costs " + pluginRef.getRate() + ", but you only have " + pluginRef.iConomy.getBank().getAccount(e.getPlayer().getName()).getBalance());
                        e.setCancelled(true);
                    }
                }
                else
                {
                    String player = e.getPlayer().getName();
                    MysqlBlock mb = new MysqlBlock(e.getBlockPlaced(), player, null, null);
                    pluginRef.getMysqlDatabase().addBlock(mb);
                    debugMessage("Block placed - not with iConomy");
                }
            }
        }
    }
    
    private void debugMessage(String str)
    {
        pluginRef.debugMessage(str);
    }

}