package me.cvenomz.OwnBlocks;

import java.sql.Time;
import java.util.ArrayList;

import org.bukkit.block.Block;

public class MysqlBlock {

    private int x,y,z;
    private String owner,tag,world;
    private ArrayList<String> allowed;
    private Time time;
    
    
    public MysqlBlock(String world, int x, int y, int z, String owner, String allowed, String tag, Time time)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.owner = owner;
        this.allowed = parseAllowed(allowed);
        this.tag = tag;
    }
    
    public MysqlBlock(Block b, String owner, String allowed, String tag)
    {
        x = b.getX();
        y = b.getY();
        z = b.getZ();
        this.world = b.getWorld().getName();
        this.owner = owner;
        this.allowed = parseAllowed(allowed);
        this.tag = tag;
    }
    
    /*
     * Only checks (x,y,z) and world
     */
    public boolean equals(Object obj)
    {
        if (obj instanceof MysqlBlock)
        {
            MysqlBlock mb = (MysqlBlock) obj;
            if (mb.x == x && mb.y == y && mb.z == z && mb.world == world)
                return true;
        }
        return false;
    }
    
    public int hashCode()
    {
        return x+y+z;
    }
    
    /*public String toString()
    {
        return world + " : ("+x+","+y+","+z+") owned by " + owner;
    }*/
    
    private ArrayList<String> parseAllowed(String str)
    {
        if (str == null)
            return new ArrayList<String>();
        String delims = "[, ]";
        String [] tokens = str.split(delims);
        ArrayList<String> ret = new ArrayList<String>();
        for (int i=0; i<tokens.length;i++)
        {
            //System.out.println(i+" - "+tokens[i]);
            if (!tokens[i].equals(""))
                ret.add(tokens[i]);
        }
        //System.out.println(tokens.length+" , "+ret);
        return ret;
    }
    
    //gets
    public String getOwner() {return owner;}
    public String getWorld() {return world;}
    public int getX() {return x;}
    public int getY() {return y;}
    public int getZ() {return z;}
    public String getTag() {return tag;}
    public String getAllowed()
    {
        String str = "";
        for (String s : allowed)
        {
            str += s;
            str += " ";
        }
        return str;
    }
    
    //get SQL string
    public String toSQLString()
    {
        String value =  "('"+
                        world+"',"+
                        x+","+
                        y+","+
                        z+",'"+
                        owner+"','"+
                        getAllowed()+"','"+
                        tag+
                        "')";
        return value;
    }
    
    public String toString()
    {
        String str =    "(" + x + "," + y + "," + z + ")";
        return str;
    }
}
