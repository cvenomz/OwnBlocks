package me.cvenomz.OwnBlocks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class ConfigManager {

	public enum StatusMessage{ENABLE,DISABLE,SIMPLE}
	
	private File propertiesFile;
	private Properties properties;
	private Logger log;
	
	//variables
	private ArrayList<Integer> exclude;
	private boolean useiConomy;
	private int iConomyRate;
	private boolean debug;
	private StatusMessage statusMessage;
	private int infoID, addID;
	private boolean enabledOnLogin;
	//MySQL variables
	private boolean useMySQL;
	private String host;
	private String databaseName;
	private String username;
	private String password;
	
	
	public ConfigManager(File file)
	{
		propertiesFile = file;
		log = Logger.getLogger("Minecraft");  //TODO: get rid of this eventually.  I think
	}
	
	//Getter Methods
	public boolean getEnabledOnLogin() {return enabledOnLogin;}
	public int getInfoID() {return infoID;}
	public int getAddID() {return addID;}
	public boolean useiConomy() {return useiConomy;}
	public int getRate() {return iConomyRate;}
	public boolean isDebug() {return debug;}
	public StatusMessage getStatusMessage() {return statusMessage;}
	public boolean useMySQL() {return useMySQL;}
	
	public void initialize()
	{
		try {
            if (!propertiesFile.exists())
                createExamplePropertiesFile();
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.severe("[OwnBlocks] Could not create or read properties file");
            e.printStackTrace();
        }
		readProperties();
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
						"\n\n#Id of material that when used will display the owner of a placed block." +
						"\n#default value is 269, which is a wooden shovel" +
						"\ninfo-id=269" +
						"\n\n#Id of material that when used will add block to the database to be protected" +
						"\n#default value is 268, which is a wooden sword" +
						"\nadd-id=268" +
						"\n\n#Set whether OwnBlocks is activated on player login (enabled by default) or if" +
						"\n#players must enable OwnBlocks themselves (disabled by default)" +
						"\nenabled-on-login=true" +
						"\n\n\n#MySQL CONFIG" +
						"\nuseMySQL=false" +
						"\nhost=localhost" +
						"\ndatabaseName=db" +
						"\nusername=user" +
						"\npassword=password");
			pw.close();
		} catch (IOException e) {
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
		if (str == null)
			statusMessage = StatusMessage.ENABLE;
		else
		{
			if (str.equalsIgnoreCase("enable"))
				statusMessage = StatusMessage.ENABLE;
			else if (str.equalsIgnoreCase("disable"))
				statusMessage = StatusMessage.DISABLE;
			else if (str.equalsIgnoreCase("simple"))
				statusMessage = StatusMessage.SIMPLE;
		}
		
		//get info tool ID
		str = properties.getProperty("info-id");
		if (str == null)
			infoID = 269;
		else
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
		if (str == null)
			addID = 268;
		else
		{
			try{
				addID = Integer.parseInt(str);
			}catch (NumberFormatException e)
			{
				log.severe("[OwnBlocks] add-id not a number.  Defaulting to 268.");
			}
		}
		
		//get enabled-on-login
		str = properties.getProperty("enabled-on-login");
		if (str != null)
		{
			if (str.equalsIgnoreCase("false"))
				enabledOnLogin = false;
		}
		
		//get MySQL config stuff
		str = properties.getProperty("useMySQL");
		if (str != null)
		{
		    if (str.equalsIgnoreCase("true"))
		    {
		        useMySQL = true;
		        host = properties.getProperty("host");
		        databaseName = properties.getProperty("databaseName");
		        username = properties.getProperty("username");
		        password = properties.getProperty("password");
		    }
		}
		
	}
}
