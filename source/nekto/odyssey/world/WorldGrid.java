package nekto.odyssey.world;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import nekto.odyssey.entity.EntityBlock;
import nekto.odyssey.entity.EntityBlockConsole;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.logging.LogAgent;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.EnumGameType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

public class WorldGrid extends World implements IBlockAccess 
{
	public World worldObj;
	
	public HashSet<EntityBlock> gridBlocks = new HashSet<EntityBlock>();
	public HashSet<EntityBlock> collidableGridBlocks = new HashSet<EntityBlock>();
	private HashMap<CoordinateWrapper, EntityBlock> gridBlockLookup = new HashMap<CoordinateWrapper, EntityBlock>(); 

	private int minGridX = 0;
	private int maxGridX = 0;
	private int minGridY = 0;
	private int maxGridY = 0;
	private int minGridZ = 0;
	private int maxGridZ = 0;
	
	private int[][][] blockIdLookup = new int[1][1][1]; //(Y X Z ORDERING)
	private int[][] blockHeightMap = new int[1][1]; // (X Z ORDERING)
	
	public RenderBlocks renderBlocks;
	
	public EntityBlockConsole ref = null;
	
	private AxisAlignedBB totalBoundingBox;
	private boolean totalBoundingBoxDirty = true;
	
	public WorldGrid(EntityBlockConsole ref)
	{
		super(new FakeSaveHandler(), "Fake", new FakeWorldProvider(), new WorldSettings(0, EnumGameType.NOT_SET, false, false, WorldType.DEFAULT), new Profiler(), new LogAgent("Odyssey-Client", " [ODYSSEY]", (new File("output-odyssey-client.log")).getAbsolutePath()));
		this.renderBlocks = new RenderBlocks(this);
		this.ref = ref;
		ref.setGrid(this);
		this.worldObj = ref.worldObj;
		this.worldInfo = worldObj.getWorldInfo();
		this.rand = worldObj.rand;
		addBlock(ref);
	}
	
	public void addBlock(EntityBlock child)
	{
		updateMinMaxDimensions(child.gridX, child.gridY, child.gridZ);
		addToHeightMap(child);
		
		child.setGrid(this);
		gridBlocks.add(child);
		
		if(child.canBeCollidedWith())
		{
			collidableGridBlocks.add(child);
		}
		
		blockIdLookup[child.gridY - minGridY][child.minX - minGridX][child.minZ - minGridZ] = child.blockId;
		gridBlockLookup.put(new CoordinateWrapper(child.gridX, child.gridY, child.gridZ), child);
		
		child.updatePositionFromConsole();
		
		totalBoundingBoxDirty = true;
		updateAllLightTypes(child.gridX, child.gridY, child.gridZ);
	}
	
	private void addToHeightMap(EntityBlock child)
	{
		int xIndex = child.gridX - minGridX;
		int zIndex = child.gridZ - minGridZ;
		
		if(blockHeightMap[xIndex][zIndex] < child.gridY)
		{
			blockHeightMap[xIndex][zIndex] = child.gridY;
		}
	}
	
	public void removeBlock(EntityBlock child)
	{
		child.setGrid(null);
		gridBlocks.remove(child);
		collidableGridBlocks.remove(child);
		gridBlockLookup.remove(new CoordinateWrapper(child.gridX, child.gridY, child.gridZ));
		
		totalBoundingBoxDirty = true;
		
		blockIdLookup[child.gridY - minGridY][child.gridX - minGridX][child.gridZ - minGridZ] = 0;
		child.setDead();
		
		removeFromHeightMap(child);
		updateAllLightTypes(child.gridX, child.gridY, child.gridZ);
	}
	
	private void removeFromHeightMap(EntityBlock child) 
	{
		int xIndex = child.gridX - minGridX;
		int zIndex = child.gridZ - minGridZ;
		
		//If this was not the top block, don't update anything
		if (blockHeightMap[xIndex][zIndex] != child.gridY) return;
		
		//Top block changed, traverse downward to find the new top.
		for (int y = child.gridY - minGridY; y >= 0; y--)
		{
			if (blockIdLookup[y][xIndex][zIndex] != 0)
			{
				blockHeightMap[xIndex][zIndex] = y;
				return;
			}
		}
		
		//No block was found, must be a hole all the way through
		//Set it to a large negative value
		blockHeightMap[xIndex][zIndex] = -128;
	}
	
	private final class CoordinateWrapper
	{
		private int[] d = new int[3];
		public CoordinateWrapper(int x, int y, int z)
		{ 
			d[0] = x; 
			d[1] = y; 
			d[2] = z; 
		}
		
		@Override
		public boolean equals(Object a)
		{ 
			if (a instanceof CoordinateWrapper)
			{
				return Arrays.equals(d, ((CoordinateWrapper)a).d);
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode()
		{ 
			return Arrays.hashCode(d); 
		}
	}
}
