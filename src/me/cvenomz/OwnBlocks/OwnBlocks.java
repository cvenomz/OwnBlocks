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

	public String mainDirectory = "plugins" + File.separator + "OwnBlocks";
	public Logger log = Logger.getLogger("Minecraft");
	//private File databaseFile = new File(mainDirectory + File.separator + "");
	public Map<OBBlock, String> database;
	public Map<Location, Player> pending;
	private FileInputStream fis;
	private FileOutputStream fos;
	private ObjectInputStream obi;
	private ObjectOutputStream obo;
	private File file;
	private OwnBlocksBlockListener listener;
	private double version = 2.0;
	
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
			listener = new OwnBlocksBlockListener(this);
			PluginManager pm = getServer().getPluginManager();
			pm.registerEvent(Event.Type.BLOCK_PLACE, listener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.BLOCK_BREAK, listener, Event.Priority.Normal, this);
			log.info("[OwnBlocks] version " + version + " initialized");
		}
		
	}
	
	

}
