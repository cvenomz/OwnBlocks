package me.cvenomz.OwnBlocks;

import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.iConomy.*;
import com.iConomy.system.Account;
import com.iConomy.system.Bank;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;

public class MysqlBlockListener extends BlockListener{
    
    private OwnBlocks pluginRef;
    private ConfigManager config;
    Logger log;
    boolean debug;
    
    MysqlBlockListener(OwnBlocks ob, ConfigManager conf)
    {
        pluginRef = ob;
        config = conf;
        //database = pluginRef.database;
        log = pluginRef.log;
        debug = config.isDebug();
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
                if (config.useiConomy())
                {
                    debugMessage("Use iConomy == true");
                    if (pluginRef.iConomy.hasAccount(e.getPlayer().getName()) && pluginRef.iConomy.getAccount(e.getPlayer().getName()) != null)
                    {
                        Holdings balance = pluginRef.iConomy.getAccount(e.getPlayer().getName()).getHoldings();
                        if (balance.hasEnough(config.getRate()))
                        {
                            balance.subtract(config.getRate());
                            String player = e.getPlayer().getName();
                            MysqlBlock mb = new MysqlBlock(e.getBlockPlaced(), player, null, null);
                            pluginRef.getMysqlDatabase().addBlock(mb);
                            debugMessage("acct. balance >= Rate, iConomy block placed");
                        }
                        else
                        {
                            debugMessage("acct. funds insufficient, block not placed");
                            e.getPlayer().sendMessage(ChatColor.YELLOW + "You dont have enough money to place this block it costs " + pluginRef.iConomy.format(config.getRate()) + ", but you only have " + pluginRef.iConomy.format(balance.balance()));
                            e.setCancelled(true);
                        }
                    }
                    else
                    {
                        //Player doesnt have an iConomy account, so create a new one
                        Account account = iConomy.getAccount(e.getPlayer().getName());
                        Bank bank = iConomy.getBank(e.getPlayer().getName());

                        if(bank == null) {
                            // No bank exists with that name.
                        }

                        // Current accounts the user has
                        int count = iConomy.Banks.count(e.getPlayer().getName());

                        if(count > 1 && !Constants.BankingMultiple) {
                            // Doesn't support multiple, and the user already has one.
                        }
                        String player = e.getPlayer().getName();
                        if(bank.createAccount(player)){
                            // Account created, check to see if it's their first, set it as their main bank.
                            if(count == 0) {
                                iConomy.getAccount(player).setMainBank(bank.getId());
                            }

                            return;
                        }
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