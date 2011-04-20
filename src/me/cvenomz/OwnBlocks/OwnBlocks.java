package me.cvenomz.OwnBlocks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class OwnBlocks extends JavaPlugin{

	public String mainDirectory = "OwnBlocks";
	public Logger log = Logger.getLogger("Minecraft");
	//private File databaseFile = new File(mainDirectory + File.separator + "");
	public Map<Player, ArrayList> database;
	public Map<Location, Player> pending;
	private FileInputStream fis;
	private FileOutputStream fos;
	private ObjectInputStream obi;
	private ObjectOutputStream obo;
	private File file;
	private OwnBlocksBlockListener listener;
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		try {
			fos = new FileOutputStream(file);
			obo = new ObjectOutputStream(fos);
			obo.writeObject(database);
			obo.close();
			fos.close();
			log.info("[OwnBlocks] Wrote database to file");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void onEnable() {
		// TODO Auto-generated method stub
		new File(mainDirectory).mkdirs();
		
		try {
				file = new File(mainDirectory + File.separator + "Database.db");
				if (file.exists())
				{
					fis = new FileInputStream(file);
					obi = new ObjectInputStream(fis);
					database = (Map<Player, ArrayList>) obi.readObject();
					fis.close();
					obi.close();
				}
				else
				{
					log.info("[OwnBlocks] Database does not exist.  Creating initial database...");
					database = new HashMap<Player, ArrayList>();
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
			listener = new OwnBlocksBlockListener(this);
			PluginManager pm = getServer().getPluginManager();
			pm.registerEvent(Event.Type.BLOCK_PLACE, listener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.BLOCK_BREAK, listener, Event.Priority.Normal, this);
			log.info("[OwnBlocks] version 1.0 initialized");
		}
		
	}
	
	/* This method will read the ArrayList containing the list of blocks this player has created from their corresponding .db file,
	 * and add this ArrayList to the main database (the Map<Player, ArrayList> called "database").
	 * If this file does not exist, then a new, blank ArrayList will be added to the main database. 
	 * 
	 * False is returned if the file cannot be read*/
	/*private boolean addToMap(Player player)
	{
		if (database.containsKey(player))
			return true;
		try {
			ArrayList playerDB;
			file = new File(mainDirectory + File.separator + player.getName() + ".db");
			fis = new FileInputStream(file);
			obi = new ObjectInputStream(fis);
			if (file.exists())
				playerDB = (ArrayList)obi.readObject();
			else
				playerDB = new ArrayList();
			database.put(player, playerDB);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.severe("Could not read player to database");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			log.severe("Could not read player to database");
			e.printStackTrace();
		}
		return false;
	}*/
	
	/* This method will take a player's ArrayList listing the blocks they have created from the main database and write it to their .db file.
	 * If this file does not exist, then it will be created.
	 * 
	 * Exception: If an exception is thrown during this process, this method will try to keep the players ArrayList still in RAM*/
	/*private void removeFromMap(Player player)
	{
		try {
			file = new File(mainDirectory + File.separator + player.getName() + ".db");
			if (!file.exists())
				file.createNewFile();
			fos = new FileOutputStream(file);
			obo = new ObjectOutputStream(fos);
			obo.writeObject(database.get(player));
			database.remove(player);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.severe("Could not write player data to file");
			e.printStackTrace();
		}

	}*/

}
