package nekto.math.traversal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.world.World;

public class Traverser
{
	private int[] currNode = new int[3];
	private Map<Integer[], Boolean> blockList = new HashMap<Integer[], Boolean>();
	private int blockType;
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
	
	public Traverser(int x, int y, int z, int block, World par1World)
	{
		this.world = par1World;
        
        this.currNode[0] = x;
        this.currNode[1] = y;
        this.currNode[2] = z;
                
        this.blockType = block;
        
        beginTraversal();
	}
	
	private void beginTraversal()
	{
		while(true)
		{
			for(int i = 0; i < 6; i++)
			{
				int[] currPos = new int[]{currNode[0] + neighbors[i][0], currNode[1] + neighbors[i][1], currNode[2] + neighbors[i][2]};
				
				if(world.getBlockId(currNode[0], currNode[1], currNode[2]) == blockType)
				{
					if(!blockList.containsValue(convertInt(currNode)))
					{
						blockList.put(convertInt(currNode), Boolean.valueOf(false));
					}
				}
				
				if(world.getBlockId(currPos[0], currPos[1], currPos[2]) == blockType)
				{
					if(!blockList.containsValue(convertInt(currPos)))
					{
						blockList.put(convertInt(currPos), Boolean.valueOf(false));
					}
				}
			}
						
			if(blockList.containsKey(false))
			{
				int[] temp = getNewNode();
				
				if(temp != null)
				{
					currNode = temp;
					System.out.println("Set new currNode!");
					blockList.remove(convertInt(temp));
					blockList.put(convertInt(currNode), Boolean.valueOf(true));
				} else {
					break;
				}
				
				/*count++;
				if(count > 5)
				{
					break;
				}*/
			} else {
				break;
			}
		}
		
		System.out.println("" + blockList.toString());
				
		Iterator<Entry< Integer[], Boolean>> it = blockList.entrySet().iterator();
		
		while(it.hasNext())
		{
			Integer[] block = (Integer[]) ((Map.Entry< Integer[], Boolean>) it.next()).getKey();
			world.setBlock(block[0], block[1], block[2], 3);
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
	
	int[] getNewNode()
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
			System.out.println("Gave new Node: " + temp.toString());
			return convertInteger(temp);
		} else {
			return null;
		}		
	}
}
