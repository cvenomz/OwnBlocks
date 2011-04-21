package me.cvenomz.OwnBlocks;

import java.io.Serializable;

import org.bukkit.block.Block;

//OBBlock is better thought of as 'OBLocation'
public class OBBlock implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int x,y,z;
	
	
	public OBBlock(Block b)
	{
		x=b.getX();
		y=b.getY();
		z=b.getZ();
	}
	
	public boolean equals(Object obj)
	{
		//if (!obj.getClass().getName().equals("OBBlock"))
		//	return false;
		OBBlock o = (OBBlock)obj;
		if (o.x == x && o.y == y && o.z == z)
			return true;
		return false;
	}
	
	public int hashCode()
	{
		return x+y+z;
	}
	
	public String toString()
	{
		return "("+x+","+y+","+z+")";
	}

}
