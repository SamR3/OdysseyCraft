package nekto.math.traversal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import nekto.odyssey.craft.CraftManager;
import nekto.odyssey.entity.EntityBlock;
import nekto.odyssey.network.PacketManager;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
//import net.minecraft.entity.player.EntityPlayerMP;

public class Traverser
{
	private int[] currNode = new int[3];
	private Map<Integer[], Boolean> blockList = new HashMap<Integer[], Boolean>();
	private int[] blockTypes;
	private EntityPlayer player;
	private World world;
	
	int count = 0;
	
	private int[][] neighbors = 
        {
            {0, 0, 1},
            {0, 1, 0},
            {1, 0, 0},
            {0, 0, -1},
            {0, -1, 0},
            {-1, 0, 0}
        };
	
	public Traverser(int x, int y, int z, int[] block, EntityPlayer par1Player, World par2World)
	{
		this.world = par2World;
        
		this.player = par1Player;
		
        this.currNode[0] = x;
        this.currNode[1] = y;
        this.currNode[2] = z;
                
        this.blockTypes = block;
        
        beginTraversal();
	}
	
	private void beginTraversal()
	{
		while(true)
		{
			for(int i = 0; i < neighbors.length; i++)
			{
				int[] currPos = new int[]{currNode[0] + neighbors[i][0], currNode[1] + neighbors[i][1], currNode[2] + neighbors[i][2]};
				
				if(contains(world.getBlockId(currPos[0], currPos[1], currPos[2])))
				{
					if(!contains(currPos))
					{
						blockList.put(convertInt(currPos), Boolean.valueOf(false));
					}
				}
			}
						
			if(blockList.containsValue(Boolean.valueOf(false)))
			{
				Integer[] temp = getNewNode();
				
				if(temp != null)
				{
					blockList.put(temp, Boolean.valueOf(true));
					currNode = convertInteger(temp);
				} else {
					break;
				}
				
			} else {
				break;
			}
		}
				
		Iterator<Entry< Integer[], Boolean>> it = blockList.entrySet().iterator();
		
		while(it.hasNext())
		{
			Integer[] block = (Integer[]) ((Map.Entry< Integer[], Boolean>) it.next()).getKey();
			
			Side side = FMLCommonHandler.instance().getEffectiveSide();
			if (side == Side.SERVER) 
			{
			        //EntityPlayerMP playerCast = (EntityPlayerMP) player;
			} else if (side == Side.CLIENT) {
			        EntityClientPlayerMP playerCast = (EntityClientPlayerMP) player;
					playerCast.sendQueue.addToSendQueue(PacketManager.generateUpdatePacket(block[0], block[1], block[2]));
					
					//CraftManager.spawnChildEntity(new EntityBlock(player.worldObj, block[0], block[1], block[2], player.worldObj.getBlockId(block[0], block[1], block[2]), player.worldObj.getBlockMetadata(block[0], block[1], block[2])));
			}
		}
	}
	
	Integer[] convertInt(int[] array)
	{
		Integer[] newArray = new Integer[array.length];
		
		int i = 0;
		for(Integer value : array)
		{
			newArray[i++] = value.intValue();
		}
		
		return newArray;
	}
	
	int[] convertInteger(Integer[] array)
	{
		int[] newArray = new int[array.length];
		
		int i = 0;
		for(int value : array)
		{
			newArray[i++] = value;
		}
		
		return newArray;
	}
	
	boolean contains(int[] array)
	{
		for (Entry<Integer[], Boolean> e : blockList.entrySet()) 
		{
			if(Arrays.deepEquals(e.getKey(), convertInt(array)))
			{
				return true;
			}
		}
		
		return false;
	}
	
	boolean contains(int contains)
	{
		for(int i = 0; i < blockTypes.length; i++)
		{
			if(blockTypes[i] == contains)
			{
				return true;
			}
		}
		
		return false;
	}
	
	Integer[] getNewNode()
	{
		Integer[] temp = null;
		for (Entry<Integer[], Boolean> e : blockList.entrySet()) 
		{
			if(e.getValue() == Boolean.valueOf(false))
			{
				temp = e.getKey();
				break;
			}
		}
		
		if(temp != null)
		{
			//System.out.println("Gave new Node: " + temp.toString());
			return temp;
		} else {
			System.out.println("New Node was null.");
			return null;
		}		
	}
}
