package me.cvenomz.OwnBlocks;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import me.cvenomz.OwnBlocks.ConfigManager.StatusMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class OwnBlocks extends JavaPlugin{
	
	//public enum StatusMessage{ENABLE,DISABLE,SIMPLE}

	public String mainDirectory = "plugins" + File.separator + "OwnBlocksX";
	public Logger log = Logger.getLogger("Minecraft");
	//public ArrayList<String> activatedPlayers;
	public Properties properties; 
	public ArrayList<Integer> exclude;
	private File propertiesFile;
	private ConfigManager configManager;
	private BlockListener blockListener;
	private PlayerListener playerListener;
	private ServerListener serverListener;
	private PermissionHandler permissions;
	public iConomy iConomy;
	//public boolean debug = false;
	//public StatusMessage statusMessage = StatusMessage.ENABLE;
	//private boolean useMySQL = false;
	private MysqlDatabase mysqlDatabase;
	private String host,databaseName,username,password;
	private String version = "0.2.1";
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		if (!configManager.useMySQL())
		{
		    log.info("[OwnBlocksX] Going to try to write database to file...");
		}
		else
		{
            try {
                mysqlDatabase.closeConnection();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                log.severe("[OwnBlocksX] Could not close database connection properly");
                e.printStackTrace();
            }
		}
		
	}

	@Override
	public void onEnable() {
	    new File(mainDirectory).mkdirs();
	    properties = new Properties();
        propertiesFile = new File(mainDirectory + File.separator + "OwnBlocksX.properties");
        
        exclude = new ArrayList<Integer>();
        configManager = new ConfigManager(propertiesFile);
        configManager.initialize();
        //readProperties();
        
        debugMessage("useMySQL = " + configManager.useMySQL());
        
        //if (useMySQL)
            yesMysqlEnable();
        //else
        //    noMysqlEnable();
	}
	
	private void yesMysqlEnable()
	{
        /*if (activatedPlayers == null)   //will be null if starting up, but not for a reload?
        {
            activatedPlayers = new ArrayList<String>();
            //addCurrentPlayers();  Disabled adding players onEnable()
        }*/
        
        //TODO: using MySQL, so we dont need database saving
        
        host = configManager.getHost();
        databaseName = configManager.getDatabaseName();
        username = configManager.getUsername();
        password = configManager.getPassword();
        mysqlDatabase = new MysqlDatabase(this, host, databaseName, username, password);
        try {
            mysqlDatabase.establishConnection();
            mysqlDatabase.CheckOBTable();
            mysqlDatabase.CheckPlayersTable();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.severe("[OwnBlocksX] Cant initialize MySQL");
            e.printStackTrace();
        }
        
        blockListener = new MysqlBlockListener(this, configManager, mysqlDatabase);
        playerListener = new MysqlPlayerListener(this, configManager);
        serverListener = new OBServerListener(this);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
        pm.registerEvent(Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
        setupPermissions();
        
        OBThread obThread = new OBThread(mysqlDatabase);
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, obThread, 72000, 72000);
        
        log.info("[OwnBlocksX] version " + version + " initialized with MySQL");
    
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
			/*if (!activatedPlayers.contains(name))
			{
				activatedPlayers.add(name);
				if (configManager.getStatusMessage() == StatusMessage.ENABLE)
					getServer().getPlayer(name).sendMessage(ChatColor.GREEN + name + ": OwnBlocks activated; Blocks you build will be protected");
				else if (configManager.getStatusMessage() == StatusMessage.SIMPLE)
					getServer().getPlayer(name).sendMessage(ChatColor.GREEN + "OwnBlocks activated");
	
			}*/
		    if (!mysqlDatabase.hasPlayer(name))
		        mysqlDatabase.addPlayer(name);
		    mysqlDatabase.setActivated(name, true);
		    
		    if (configManager.getStatusMessage() == StatusMessage.ENABLE)
                getServer().getPlayer(name).sendMessage(ChatColor.GREEN + name + ": OwnBlocks activated; Blocks you build will be protected");
            else if (configManager.getStatusMessage() == StatusMessage.SIMPLE)
                getServer().getPlayer(name).sendMessage(ChatColor.GREEN + "OwnBlocks activated");
		}
	}
	
	public void removePlayer(String name)
	{
		/*if (activatedPlayers.contains(name))
		{
			activatedPlayers.remove(name);
			if (configManager.getStatusMessage() == StatusMessage.ENABLE)
				getServer().getPlayer(name).sendMessage(ChatColor.AQUA + name + ": OwnBlocks now deactivated");
			if (configManager.getStatusMessage() == StatusMessage.SIMPLE)
				getServer().getPlayer(name).sendMessage(ChatColor.AQUA + "OwnBlocks deactivated");
		}*/
	    if (mysqlDatabase.getPlayer(name) != null)
	    {
	        mysqlDatabase.setActivated(name, false);
	        if (configManager.getStatusMessage() == StatusMessage.ENABLE)
                getServer().getPlayer(name).sendMessage(ChatColor.AQUA + name + ": OwnBlocks now deactivated");
            if (configManager.getStatusMessage() == StatusMessage.SIMPLE)
                getServer().getPlayer(name).sendMessage(ChatColor.AQUA + "OwnBlocks deactivated");
	    }
	}
	
	private void togglePlayer(String name)
	{
		if (mysqlDatabase.isPlayerActivated(name))
			removePlayer(name);
		else
			addPlayer(name);
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		String commandName = command.getName().toLowerCase();
		
		if (!(sender instanceof Player))
		{
			//log.info("OwnBlocks Activated Players: " + activatedPlayers.toString());
			return false;
		}
		Player p = (Player)sender;
		String player = p.getName();
		//log.info("\""+player+"\"");
		if (commandName.equalsIgnoreCase("ownblocksx") || commandName.equalsIgnoreCase("obx"))
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
			log.info("[OwnBlocksX] " + str);
	}
	
	public boolean hasPermission(Player p, String node)
	{
		if (permissions != null)
		{
			return permissions.has(p, node);
		}
		else
		{
			if (node.equals("OwnBlocksX.use"))
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
