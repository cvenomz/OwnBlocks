package me.cvenomz.OwnBlocks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.coelho.iConomy.iConomy;

public class OwnBlocks extends JavaPlugin{
	
	public enum StatusMessage{ENABLE,DISABLE,SIMPLE}

	public String mainDirectory = "plugins" + File.separator + "OwnBlocksMySQL";
	public Logger log = Logger.getLogger("Minecraft");
	public ArrayList<String> activatedPlayers;
	public Properties properties; 
	public ArrayList<Integer> exclude;
	private File propertiesFile;
	private ConfigManager configManager;
	private BlockListener blockListener;
	private PlayerListener playerListener;
	private PermissionHandler permissions;
	public iConomy iConomy;
	//public boolean debug = false;
	public StatusMessage statusMessage = StatusMessage.ENABLE;
	private boolean useMySQL = false;
	private MysqlDatabase mysqlDatabase;
	private String host,databaseName,username,password;
	private String version = "0.1";
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		if (!useMySQL)
		{
		    log.info("[OwnBlocks] Going to try to write database to file...");
		}
		else
		{
            try {
                mysqlDatabase.closeConnection();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                log.severe("[OwnBlocks] Could not close database connection properly");
                e.printStackTrace();
            }
		}
		
	}

	@Override
	public void onEnable() {
	    new File(mainDirectory).mkdirs();
	    properties = new Properties();
        propertiesFile = new File(mainDirectory + File.separator + "OwnBlocks.properties");
        
        exclude = new ArrayList<Integer>();
        configManager = new ConfigManager(propertiesFile);
        //readProperties();
        
        debugMessage("useMySQL = " + useMySQL);
        
        //if (useMySQL)
            yesMysqlEnable();
        //else
        //    noMysqlEnable();
	}
	
	private void yesMysqlEnable()
	{
        if (activatedPlayers == null)   //will be null if starting up, but not for a reload?
        {
            activatedPlayers = new ArrayList<String>();
            //addCurrentPlayers();  Disabled adding players onEnable()
        }
        
        //TODO: using MySQL, so we dont need database saving
        
        
        mysqlDatabase = new MysqlDatabase(this, host, databaseName, username, password);
        try {
            mysqlDatabase.establishConnection();
            mysqlDatabase.CheckOBTable();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.severe("[OwnBlocks] Cant initialize MySQL");
            e.printStackTrace();
        }
        
        blockListener = new MysqlBlockListener(this, configManager);
        playerListener = new MysqlPlayerListener(this, configManager);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
        setupPermissions();
        log.info("[OwnBlocks] version " + version + " initialized with MySQL");
    
	}
		
	private void addCurrentPlayers()
	{
		Player[] players = getServer().getOnlinePlayers();
		for (int i=0; i < players.length; i++)
		{
			addPlayer(players[i].getName());
		}
	}
		
	public void addPlayer(String name)
	{
		if (hasPermission(name, "OwnBlocks.use"))
		{
			if (!activatedPlayers.contains(name))
			{
				activatedPlayers.add(name);
				if (statusMessage == StatusMessage.ENABLE)
					getServer().getPlayer(name).sendMessage(ChatColor.GREEN + name + ": OwnBlocks activated; Blocks you build will be protected");
				else if (statusMessage == StatusMessage.SIMPLE)
					getServer().getPlayer(name).sendMessage(ChatColor.GREEN + "OwnBlocks activated");
	
			}
		}
	}
	
	public void removePlayer(String name)
	{
		if (activatedPlayers.contains(name))
		{
			activatedPlayers.remove(name);
			if (statusMessage == StatusMessage.ENABLE)
				getServer().getPlayer(name).sendMessage(ChatColor.AQUA + name + ": OwnBlocks now deactivated");
			if (statusMessage == StatusMessage.SIMPLE)
				getServer().getPlayer(name).sendMessage(ChatColor.AQUA + "OwnBlocks deactivated");
		}
	}
	
	private void togglePlayer(String name)
	{
		if (activatedPlayers.contains(name))
			removePlayer(name);
		else
			addPlayer(name);
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		String commandName = command.getName().toLowerCase();
		
		if (!(sender instanceof Player))
		{
			log.info("OwnBlocks Activated Players: " + activatedPlayers.toString());
			return false;
		}
		Player p = (Player)sender;
		String player = p.getName();
		//log.info("\""+player+"\"");
		if (commandName.equalsIgnoreCase("ownblocks") || commandName.equalsIgnoreCase("ob"))
		{
			togglePlayer(player);
			return true;
		}
		
		return false;
	}
	
	private void setupPermissions()
	{
		Plugin permRef = this.getServer().getPluginManager().getPlugin("Permissions");
		if (permissions == null)
		{
			if (permRef != null)
				permissions = ((Permissions)permRef).getHandler();
			else
				log.info("Permission system not detected, defaulting to OP");
		}
	}
	
	public void debugMessage(String str)
	{
		if (configManager.isDebug())
			log.info("[OwnBlocks] " + str);
	}
	
	public boolean hasPermission(Player p, String node)
	{
		if (permissions != null)
		{
			return permissions.has(p, node);
		}
		else
		{
			if (node.equals("OwnBlocks.use"))
				return true;					//Default to all players able to protect blocks
			
			return p.isOp();
		}
	}
	
	public boolean hasPermission(String player, String node)
	{
		return hasPermission(getServer().getPlayer(player), node);
	}
	
	public MysqlDatabase getMysqlDatabase()
	{
	    return mysqlDatabase;
	}
	
	

}
