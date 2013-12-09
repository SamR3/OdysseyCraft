package nekto.math.traversal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.world.World;

public class Traverser
{
	private BlockPositionWrapper currNode = new BlockPositionWrapper();
	private Map<BlockPositionWrapper, Boolean> blockList = new HashMap<BlockPositionWrapper, Boolean>();
	private int[] blockTypes;
	private World world;

	int count = 0;

	private int[][] neighbors = { { 0, 0, 1 }, { 0, 1, 0 }, { 1, 0, 0 },
			{ 0, 0, -1 }, { 0, -1, 0 }, { -1, 0, 0 } };

	public Traverser(int x, int y, int z, World par2World)
	{
		this.world = par2World;

		this.currNode.x = x;
		this.currNode.y = y;
		this.currNode.z = z;

		this.blockTypes = new int[]{1};
	}

	public HashSet<BlockPositionWrapper> beginTraversal()
	{
		while (true)
		{
			for (int i = 0; i < neighbors.length; i++)
			{
				BlockPositionWrapper currPos = new BlockPositionWrapper(currNode.x + neighbors[i][0],
						currNode.y + neighbors[i][1],
						currNode.z + neighbors[i][2] );

				if (contains(world.getBlockId(currPos.x, currPos.y, currPos.z)))
				{
					if (!contains(currPos))
					{
						blockList.put(currPos, Boolean.valueOf(false));
					}
				}
			}

			if (blockList.containsValue(Boolean.valueOf(false)))
			{
				BlockPositionWrapper temp = getNewNode();

				if (temp != null)
				{
					blockList.put(temp, Boolean.valueOf(true));
					currNode = new BlockPositionWrapper();
				} else
				{
					break;
				}

			} else
			{
				break;
			}
		}
				
		return new HashSet<BlockPositionWrapper>(this.blockList.keySet());
/*
		Iterator<Entry<Integer[], Boolean>> it = blockList.entrySet()
				.iterator();

		while (it.hasNext())
		{
			Integer[] block = (Integer[]) ((Map.Entry<Integer[], Boolean>) it
					.next()).getKey();

			Side side = FMLCommonHandler.instance().getEffectiveSide();
			if (side == Side.SERVER)
			{
				// EntityPlayerMP playerCast = (EntityPlayerMP) player;
			} else if (side == Side.CLIENT)
			{
				EntityClientPlayerMP playerCast = (EntityClientPlayerMP) player;
				playerCast.sendQueue.addToSendQueue(PacketManager
						.generateUpdatePacket(block[0], block[1], block[2]));
			}
		}*/
	}
	
	private boolean contains(int contains)
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

	private boolean contains(BlockPositionWrapper test)
	{
		for (Entry<BlockPositionWrapper, Boolean> e : blockList.entrySet())
		{
			if (e.getKey().equals(test))
			{
				return true;
			}
		}

		return false;
	}

	private BlockPositionWrapper getNewNode()
	{
		BlockPositionWrapper temp = null;
		
		for (Entry<BlockPositionWrapper, Boolean> e : blockList.entrySet())
		{
			if (e.getValue() == Boolean.valueOf(false))
			{
				temp = e.getKey();
				break;
			}
		}

		if (temp != null)
		{
			// System.out.println("Gave new Node: " + temp.toString());
			return temp;
		} else
		{
			return null;
		}
	}
}
