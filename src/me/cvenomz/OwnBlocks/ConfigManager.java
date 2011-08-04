package me.cvenomz.OwnBlocks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class ConfigManager {

	public enum StatusMessage{ENABLE,DISABLE,SIMPLE}
	
	private File propertiesFile;
	private Properties properties = new Properties();
	private Logger log;
	
	//variables
	private ArrayList<Integer> exclude = new ArrayList<Integer>();
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
	public String getHost() {return host;}
	public String getDatabaseName() {return databaseName;}
	public String getUsername() {return username;}
	public String getPassword() {return password;}
	
	public void initialize()
	{
		try {
            if (!propertiesFile.exists())
                createExamplePropertiesFile();
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.severe("[OwnBlocksX] Could not create or read properties file");
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
			pw.println( "\n#MySQL CONFIG" +
                        "\nhost=localhost" +
                        "\ndatabaseName=db" +
                        "\nusername=user" +
                        "\npassword=password" +
                        "\n\n\\n#Exclude certain items from being added to the database when a player places them." +
						"\n#Please note this does not retro-actively affect the database. (comma separated; no spaces)" +
						"\n#This default excludes Dirt(03), Sand(12), and Saplings(06) from being added to the database" +
						"\nexclude=03,12,06" +
						"\n" +
						"\n\n#To charge players a basic rate to their iConomy accounts, enter the amount (Integer)" +
						"\n#that you wish to charge them per block they protect. Values <= 0 disable iConomy" +
						"\niConomy=0" +
						"\n\n#Debug mode" + 
						"\ndebug=false" + 
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
						"\n");
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
				{log.severe("[OwnBlocksX] Error reading exclude IDs -- Not an Integer");}
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
					log.info("[OwnBlocksX] iConomy support activated. Rate=" + iConomyRate);
				}
				else
					useiConomy = false;
			} catch (NumberFormatException e)
			{
				log.severe("[OwnBlocksX] iConomy support cannot be activated. The rate is not a proper number");
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
				log.severe("[OwnBlocksX] info-id not a number.  Defaulting to 269.");
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
				log.severe("[OwnBlocksX] add-id not a number.  Defaulting to 268.");
			}
		}
		
		//get enabled-on-login
		str = properties.getProperty("enabled-on-login");
		if (str != null)
		{
			if (str.equalsIgnoreCase("true"))
				enabledOnLogin = true;
		}
		
		//get MySQL config stuff
        useMySQL = true;
        host = properties.getProperty("host");
        databaseName = properties.getProperty("databaseName");
        username = properties.getProperty("username");
        password = properties.getProperty("password");
		
	}
}
