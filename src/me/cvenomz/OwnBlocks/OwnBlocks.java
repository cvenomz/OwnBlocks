package me.cvenomz.OwnBlocks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.coelho.iConomy.iConomy;

public class OwnBlocks extends JavaPlugin{
	
	public enum StatusMessage{ENABLE,DISABLE,SIMPLE}

	public String mainDirectory = "plugins" + File.separator + "OwnBlocks";
	public Logger log = Logger.getLogger("Minecraft");
	//private File databaseFile = new File(mainDirectory + File.separator + "");
	public Map<OBBlock, String> database;
	public ArrayList<String> activatedPlayers;
	public Properties properties; 
	public ArrayList<Integer> exclude;
	private FileInputStream fis;
	private FileOutputStream fos;
	private ObjectInputStream obi;
	private ObjectOutputStream obo;
	private File file;
	private File tmpDatabaseFile;
	private File propertiesFile;
	private boolean useiConomy;
	private int iConomyRate;
	private OwnBlocksBlockListener blockListener;
	private OwnBlocksPlayerListener playerListener;
	private PermissionHandler permissions;
	public iConomy iConomy;
	public boolean debug = false;
	public StatusMessage statusMessage = StatusMessage.ENABLE;
	private SaveDatabase saveDatabaseRef;
	private int infoID = 269;
	private int addID = 268;
	private boolean enabledOnLogin = true;
	private double version = 10.0;
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		log.info("[OwnBlocks] Going to try to write database to file...");
		writeDatabaseToFile();
		
		
	}

	@Override
	public void onEnable() {
		// TODO Auto-generated method stub
		new File(mainDirectory).mkdirs();
		
		try {
				file = new File(mainDirectory + File.separator + "Database.db");
				tmpDatabaseFile = new File(mainDirectory + File.separator + "Database_tmp.db");
				if (file.exists())
				{
					fis = new FileInputStream(file);
					obi = new ObjectInputStream(fis);
					database = (Map<OBBlock, String>) obi.readObject();
					log.info("[OwnBlocks] Database read in from file");
					fis.close();
					obi.close();
				}
				else
				{
					log.info("[OwnBlocks] Database does not exist.  Creating initial database...");
					database = new HashMap<OBBlock, String>();
				}
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		if (database != null)
		{
			properties = new Properties();
			propertiesFile = new File(mainDirectory + File.separator + "OwnBlocks.properties");
			try {
				if (!propertiesFile.exists())
					createExamplePropertiesFile();
				properties.load(new FileInputStream(propertiesFile));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.severe("[OwnBlocks] Could not create or read properties file");
					e.printStackTrace();
			}
			exclude = new ArrayList<Integer>();
			readProperties();
			if (activatedPlayers == null)	//will be null if starting up, but not for a reload?
			{
				activatedPlayers = new ArrayList<String>();
				//addCurrentPlayers();  Disabled adding players onEnable()
			}
			blockListener = new OwnBlocksBlockListener(this);
			playerListener = new OwnBlocksPlayerListener(this);
			PluginManager pm = getServer().getPluginManager();
			pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
			setupPermissions();
			log.info("[OwnBlocks] version " + version + " initialized");
		}
		
	}
	
	private void addCurrentPlayers()
	{
		Player[] players = getServer().getOnlinePlayers();
		for (int i=0; i < players.length; i++)
		{
			addPlayer(players[i].getName());
		}
	}
	
	/*private void removeAllPlayers()
	{
		for (String name : activatedPlayers)
		{
			getServer().getPlayer(name).sendMessage(ChatColor.DARK_PURPLE + "Server is Deactivating OwnBlocks--");
			removePlayer(name);
		}
	}*/
	
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
	
	private void createExamplePropertiesFile()
	{
		try {
			propertiesFile.createNewFile();
			PrintWriter pw = new PrintWriter(propertiesFile);
			pw.println("#OwnBlocks Properties File");
			pw.println("\n#to exclude certain items from being protected when a player places them" +
						"\n#regarldess of if they have OwnBlocks activated, enter the ID of the item" +
						"\n#after the exclude key (comma separated; no spaces)" +
						"\n#The example below would exclude Dirt(03) and Sand(12) from being added to the database" +
						"\n#\n#exclude=03,12" +
						"\n" +
						"\n#Please Note: changes are not retro-active. In this example, dirt placed before being excluded" +
						"\n#Would still be protected, even after it is added to the 'exclude list'" +
						"\n\n#To charge players a basic rate to their iConomy accounts, enter the amount (Integer)" +
						"\n#that you wish to charge them per block they protect. Values <= 0 disable iConomy" +
						"\niConomy=0" +
						"\n\n#Uncomment to enable debug mode" + 
						"\n#debug=true" + 
						"\n\n#status-message is the message sent to players telling them when OwnBlocks has" +
						"\n#been activated or deactivated for them. Options are: [enable, disable, simple]" +
						"\nstatus-message=enable" +
						"\n\n#Number of seconds between database saves.  '0' to disable (not reccommended)" +
						"\nsave-interval=60" + 
						"\n\n#Id of material that when used will display the owner of a placed block." +
						"\n#default value is 269, which is a wooden shovel" +
						"\ninfo-id=269" +
						"\n\n#Id of material that when used will add block to the database to be protected" +
						"\n#default value is 268, which is a wooden sword" +
						"\nadd-id=268" +
						"\n\n#Set whether OwnBlocks is activated on player login (enabled by default) or if" +
						"\n#players must enable OwnBlocks themselves (disabled by default)" +
						"\nenabled-on-login=true");
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void readProperties()
	{
		//get exclude string
		String str = properties.getProperty("exclude");
		if (str != null)
		{
			StringTokenizer st = new StringTokenizer(str, ",");
			while (st.hasMoreTokens())
			{
				try {
				exclude.add(Integer.parseInt(st.nextToken()));
				}catch (NumberFormatException e)
				{log.severe("[OwnBlocks] Error reading exclude IDs -- Not an Integer");}
			}
		}
		
		//get iConomy string
		str = properties.getProperty("iConomy");
		int tmp;
		if (str != null)
		{
			try{
				tmp = Integer.parseInt(str);
				if (tmp > 0)
				{
					useiConomy = true;
					iConomyRate = tmp;
					log.info("[OwnBlocks] iConomy support activated. Rate=" + iConomyRate);
				}
				else
					useiConomy = false;
			} catch (NumberFormatException e)
			{
				log.severe("[OwnBlocks] iConomy support cannot be activated. The rate is not a proper number");
				useiConomy = false;
			}
		}
		else
			useiConomy = false;
		
		//get debug
		str = properties.getProperty("debug");
		if (str != null && str.equalsIgnoreCase("true"))
			debug = true;
		
		//get status-message
		str = properties.getProperty("status-message");
		if (str != null)
		{
			if (str.equalsIgnoreCase("enable"))
				statusMessage = StatusMessage.ENABLE;
			else if (str.equalsIgnoreCase("disable"))
				statusMessage = StatusMessage.DISABLE;
			else if (str.equalsIgnoreCase("simple"))
				statusMessage = StatusMessage.SIMPLE;
		}
		
		//get save-interval
		str = properties.getProperty("save-interval");
		if (str != null)
		{
			saveDatabaseRef = new SaveDatabase(this);
			long interval = 60*20L;
			try{
				interval = 20*Long.parseLong(str);
			}catch (NumberFormatException e) {log.severe("[OwnBlocks] save-interval not a number, defaulting to 60s");}
			if (interval > 0)
			{
				int code = getServer().getScheduler().scheduleSyncRepeatingTask(this, saveDatabaseRef, interval, interval);
				//log.info("Code: "+code); //debug
			}
		}
		
		//get info tool ID
		str = properties.getProperty("info-id");
		if (str != null)
		{
			try{
				infoID = Integer.parseInt(str);
			}catch (NumberFormatException e)
			{
				log.severe("[OwnBlocks] info-id not a number.  Defaulting to 269.");
			}
		}
		
		//get add tool ID
		str = properties.getProperty("add-id");
		if (str != null)
		{
			try{
				addID = Integer.parseInt(str);
			}catch (NumberFormatException e)
			{
				log.severe("[OwnBlocks] info-id not a number.  Defaulting to 269.");
			}
		}
		
		//get enabled-on-login
		str = properties.getProperty("enabled-on-login");
		if (str != null)
		{
			if (str.equalsIgnoreCase("false"))
				enabledOnLogin = false;
		}
		
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
	
	private void setupiConomy()
	{
		Plugin iConRef = this.getServer().getPluginManager().getPlugin("iConomy");
		if (iConomy == null)
		{
			if (iConRef != null)
				iConomy = ((iConomy)iConRef);
			else
			{
				log.info("[OwnBlocks] iConomy not detected. No iConomy integration");
				useiConomy = false;
			}
		}
	}
	
	public boolean useiConomy()
	{
		return useiConomy;
	}
	
	public int getRate()
	{
		return iConomyRate;
	}
	
	public void debugMessage(String str)
	{
		if (debug)
			log.info("[OwnBlocks] " + str);
	}
	
	public boolean writeDatabaseToFile()
	{
		boolean bool = true;
		try {
			fos = new FileOutputStream(tmpDatabaseFile);
			obo = new ObjectOutputStream(fos);
			obo.writeObject(database);
			obo.close();
			fos.close();
			debugMessage("Wrote database to tmp file");
			file.delete();
			tmpDatabaseFile.renameTo(file);
			debugMessage("Deleted original database and renamed tmp");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			bool = false;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			bool = false;
			e.printStackTrace();
		}
		
		return bool;
	}
	
	public int getInfoID()
	{
		return infoID;
	}
	
	public int getAddID()
	{
		return addID;
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
	
	public boolean getEnabledOnLogin()
	{
		return enabledOnLogin;
	}
	
	

}
