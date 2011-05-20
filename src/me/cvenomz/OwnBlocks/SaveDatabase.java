package me.cvenomz.OwnBlocks;

import org.bukkit.plugin.Plugin;

public class SaveDatabase implements Runnable{

	private OwnBlocks pluginRef;
	
	public SaveDatabase(OwnBlocks p)
	{
		pluginRef = p;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		pluginRef.debugMessage("Writing database to file...");
		if (pluginRef.writeDatabaseToFile())
			pluginRef.debugMessage("Database written");
		else
			pluginRef.debugMessage("Error while writing database");
	}

}
