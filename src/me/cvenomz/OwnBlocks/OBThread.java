package me.cvenomz.OwnBlocks;

import java.util.logging.Logger;

public class OBThread implements Runnable{
    
    private MysqlDatabase database;
    private Logger log;
    
    public OBThread(MysqlDatabase md)
    {
        database = md;
        log = Logger.getLogger("Minecraft");
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            database.resetConnection();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.severe("[OwnBlocksX] Could not reset database connection");
            e.printStackTrace();
        }
    }

}
