package nekto.odyssey.world;

import java.awt.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import nekto.odyssey.entity.EntityBlock;
import nekto.odyssey.entity.EntityBlockConsole;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.logging.LogAgent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumGameType;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGrid extends World implements IBlockAccess 
{
	public World worldObj;
	
	public HashSet<EntityBlock> gridBlocks = new HashSet<EntityBlock>();
	public HashSet<EntityBlock> collidableGridBlocks = new HashSet<EntityBlock>();
	private HashMap<CoordinateWrapper, EntityBlock> gridBlockLookup = new HashMap<CoordinateWrapper, EntityBlock>(); 
	private HashMap<CoordinateWrapper, Integer> skylightMap = new HashMap<CoordinateWrapper, Integer>();
	private HashMap<CoordinateWrapper, Integer> blocklightMap = new HashMap<CoordinateWrapper, Integer>();

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
		
		blockIdLookup[child.gridY - minGridY][child.gridX - minGridX][child.gridZ - minGridZ] = child.blockId;
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
	
	@Override
	public int getHeightValue(int i, int k)
	{
		int xIndex = i-minGridX;
		int zIndex = k-minGridZ;
		
		if (xIndex < 0 || zIndex < 0 || xIndex >= blockHeightMap.length || zIndex >= blockHeightMap[xIndex].length)
		{
			return -128;
		}
		
		return blockHeightMap[xIndex][zIndex];
		
	}

	public void clear()
	{
		gridBlocks.clear();
		collidableGridBlocks.clear();
		gridBlockLookup.clear();
		totalBoundingBoxDirty = true;
		blockIdLookup = new int[1][1][1];
	}
	
	public int size()
	{
		return gridBlocks.size();
	}
	
	public EntityBlock getBlock(int x, int y, int z)
	{
		return gridBlockLookup.get(new CoordinateWrapper(x, y, z));
	}
	
	private void updateMinMaxDimensions(int x, int y, int z)
	{
		int xDimensionChanged = 0;
		int yDimensionChanged = 0;
		int zDimensionChanged = 0;
		
		if (x > maxGridX)
		{
			xDimensionChanged = x - maxGridX;
			maxGridX = x;
		}
		if (x < minGridX)
		{
			xDimensionChanged = x - minGridX;
			minGridX = x;
		}
		if (y > maxGridY)
		{
			yDimensionChanged = y - maxGridY;
			maxGridY = y;
		}
		if (y < minGridY) 
		{
			yDimensionChanged = y - minGridY;
			minGridY = y;
		}
		if (z > maxGridZ)
		{
			zDimensionChanged = z - maxGridZ;
			maxGridZ = z;
		}
		if (z < minGridZ) 
		{
			zDimensionChanged = z - minGridZ;
			minGridZ = z;
		}
		
		if (xDimensionChanged != 0 || yDimensionChanged != 0 || zDimensionChanged != 0) 
		{
			resizeLookupArrays(xDimensionChanged, yDimensionChanged, zDimensionChanged);
		}
	}
	
	/*
	 * Takes in delta dimensions for the lookup arrays, and resizes them.
	 * If the delta is positive, the new array will contain data in the same place
	 * as the old array (the new space is at the end of the array).
	 * If the the delta is negative, the new array will contain the data shifted up by the delta
	 * so the new area is at the beginning of the array.
	 */
	private void resizeLookupArrays(int xDelta, int yDelta, int zDelta)
	{
		int newYDim = blockIdLookup.length + Math.abs(yDelta);
		int newXDim = blockIdLookup[0].length + Math.abs(xDelta);
		int newZDim = blockIdLookup[0][0].length + Math.abs(zDelta);
		
		int yIndexOffset = (yDelta > 0)? 0 : yDelta * -1;
		int xIndexOffset = (xDelta > 0)? 0 : xDelta * -1;
		int zIndexOffset = (zDelta > 0)? 0 : zDelta * -1;
		
		//Resize the blockIdLookup table
		int [][][] resized_blockIdLookup = new int[newYDim][newXDim][newZDim];
		for (int yIndex = 0; yIndex < blockIdLookup.length; yIndex++)
		{
			for (int xIndex = 0; xIndex < blockIdLookup[yIndex].length; xIndex++)
			{
				for (int zIndex = 0; zIndex < blockIdLookup[yIndex][xIndex].length; zIndex++)
				{
					resized_blockIdLookup[yIndex + yIndexOffset][xIndex + xIndexOffset][zIndex + zIndexOffset] = blockIdLookup[yIndex][xIndex][zIndex];
				}
			}			
		}
		blockIdLookup = resized_blockIdLookup;
		
		//Resize the heightmap
		int [][] resized_blockHeightMap = new int[newXDim][newZDim];

		for (int xIndex = 0; xIndex < blockHeightMap.length; xIndex++)
		{
			for (int zIndex = 0; zIndex < blockHeightMap[xIndex].length; zIndex++)
			{
				resized_blockHeightMap[xIndex + xIndexOffset][zIndex + zIndexOffset] = blockHeightMap[xIndex][zIndex];
			}
		}			

		blockHeightMap = resized_blockHeightMap;
	}
	
	private int worldHeight = 256;
	
	private void calculateTotalBoundingBox()
	{
		this.theProfiler.startSection("blockGrid-calculateTotalBoundingBox");
		totalBoundingBox = AxisAlignedBB.getBoundingBox(
				0, 
				0, 
				0,
				0, 
				0, 
				0);
		
		if (!collidableGridBlocks.isEmpty())
		{
			for (EntityBlock child : collidableGridBlocks)
			{
				totalBoundingBox.minX = Math.min(child.gridX, totalBoundingBox.minX);
				totalBoundingBox.minY = Math.min(child.gridY, totalBoundingBox.minY);
				totalBoundingBox.minZ = Math.min(child.gridZ, totalBoundingBox.minZ);
				
				totalBoundingBox.maxX = Math.max(child.gridX, totalBoundingBox.maxX);
				totalBoundingBox.maxY = Math.max(child.gridY, totalBoundingBox.maxY);
				totalBoundingBox.maxZ = Math.max(child.gridZ, totalBoundingBox.maxZ);
			}
		}
		
		//Make the total bounding box bigger just so we can keep control when we go on the roof
		//And so its harder to fall off
		totalBoundingBox.minX -= 1;
		totalBoundingBox.maxX += 2;
		totalBoundingBox.minY -= 1;
		totalBoundingBox.maxY += 4;
		totalBoundingBox.minZ -= 1;
		totalBoundingBox.maxZ += 2;
		this.theProfiler.endSection();
	}
	
	public AxisAlignedBB getTotalBoundingBox()
	{
		if (totalBoundingBoxDirty == true)
		{
			calculateTotalBoundingBox();
			totalBoundingBoxDirty = false;
		}
		return totalBoundingBox.copy();
	}
	
	public void updateChildPositions() 
	{
		this.theProfiler.startSection("blockGrid-updateChildPositions");
		if (gridBlocks != null && !gridBlocks.isEmpty())
		{
			for (EntityBlock subBlock : gridBlocks)
	        {
	        	subBlock.updatePositionFromConsole();
	        }
		}
		this.theProfiler.endSection();
	}
	
	public NBTTagList saveGridToNBT()
	{
		NBTTagList gridData = new NBTTagList();
		
		for (EntityBlock child : gridBlocks )
		{
			if (child != ref) gridData.appendTag(child.saveChildBlock(new NBTTagCompound()));
		}
		
		return gridData;
	}
	
	public void loadGridFromNBT(NBTTagList gridData)
	{
		for (int i = 0; i < gridData.tagCount(); i++)
		{
			EntityBlock child = EntityBlock.createChildBlockFromNBT((NBTTagCompound)gridData.tagAt(i));
			if (child != null)
			{
				addBlock(child);
			}
		}
		notifyBlocksCreation();
	}
	
	
	//
	// Collision block 
	//
	private final class CoordinateWrapper
	{
		private int[] d = new int[3];
		public CoordinateWrapper(int x, int y, int z){ d[0] = x; d[1] = y; d[2] = z; }
		@Override
		public boolean equals(Object a){ if (a instanceof CoordinateWrapper) return Arrays.equals(d, ((CoordinateWrapper)a).d); else return false; }
		@Override
		public int hashCode(){ return Arrays.hashCode(d); }
	}
	
	
	
	
	
	//
	// World routines below
	//
	
	
	@Override
	public int getBlockId(int x, int y, int z) 
	{
		if ((maxGridY >= y && y >= minGridY) 
				&& (maxGridX >= x && x >= minGridX) 
				&& (maxGridZ >= z && z >= minGridZ))
		{
			return blockIdLookup[y-minGridY][x-minGridX][z-minGridZ];
		}
		else
		{
			return 0;
		}
	}

	@Override
	public TileEntity getBlockTileEntity(int x, int y, int z) 
	{
		/*EntityBlock block = getBlock(x, y, z);
		if (block instanceof WorldGridTileEntity)
		{
			return ((WorldGridTileEntity)block).getTile();
		}
		else
		{
			return null;
		}*/
		
		return null;
	}
	
    @Override
	public void setBlockTileEntity(int x, int y, int z, TileEntity tileentity)
	{
		/*if (getBlock(x, y, z) instanceof WorldGridTileEntity)
		{
			((WorldGridTileEntity)(getBlock(x, y, z))).setTile(tileentity);
		}
		else
		{
			// Remove EntityBlock
			EntityBlock oldBlock = getBlock(x, y, z);
			removeBlock(oldBlock);
			 ... and create WorldGridTileEntity instead.
			ref.addChildTile(oldBlock.blockId, x, y, z, oldBlock.meta, tileentity);
		}*/
	}
	
    @Override
	public void removeBlockTileEntity(int i, int j, int k)
	{
		/*if (getBlock(i,j,k) instanceof WorldGridTileEntity)
		{
			((WorldGridTileEntity)(getBlock(i,j,k))).setTile(null);
		}*/
	}
    
    // LIGHTING       
    @Override
    public int getSavedLightValue(EnumSkyBlock enumskyblock, int i, int j, int k)
    {
        if (enumskyblock == EnumSkyBlock.Sky)
        {
        	Integer ret = skylightMap.get(new CoordinateWrapper(i, j, k));
        	return (ret != null)? ret.intValue() : 0;
        }
        if (enumskyblock == EnumSkyBlock.Block)
        {
        	Integer ret = blocklightMap.get(new CoordinateWrapper(i, j, k));
        	return (ret != null)? ret.intValue() : 0;
        }
        else
        {
            return 0;
        }
    }
    
    @Override
    public int getLightBrightnessForSkyBlocks(int i, int j, int k, int l)
    {
    	int realI1 = 0;
    	int realJ1 = 0;
    	
    	if (WorldGrid.dynamicLighting)
    	{
			int posX = (int) (ref.posX + i*MathHelper.cos((float) (-ref.rotationYaw/180*(Math.PI))) - k*MathHelper.sin((float) (-ref.rotationYaw/180*(Math.PI))));
			int posY = (int) (ref.posY + j);
			int posZ = (int) (ref.posZ + i*MathHelper.sin((float) (-ref.rotationYaw/180*(Math.PI))) + k*MathHelper.cos((float) (-ref.rotationYaw/180*(Math.PI))));
					
	    	int realWorldLight = worldObj.getLightBrightnessForSkyBlocks(posX,posY,posZ,0);
	        
	    	//realI1 = (realWorldLight & 0xFFF00000) >> 20;
	    	realJ1 = (realWorldLight & 0x000FFFFF) >> 4;
    	}
    	
    	int i1 = getSkyBlockTypeBrightness(EnumSkyBlock.Sky, i, j, k);
        int j1 = getSkyBlockTypeBrightness(EnumSkyBlock.Block, i, j, k);
        if (j1 < l)
        {
            j1 = l;
        }
        if (realI1 > i1) i1 = realI1;
        if (realJ1 > j1) j1 = realJ1;
        
        return i1 << 20 | j1 << 4;
        
    }
    
    @Override
    public int getSkyBlockTypeBrightness(EnumSkyBlock enumskyblock, int i, int j, int k)
    {
        if (provider.hasNoSky && enumskyblock == EnumSkyBlock.Sky)
        {
            return 0;
        }
       
        if (j >= worldHeight  && enumskyblock == EnumSkyBlock.Sky)
        {
            return 15;
        }
        if (j >= worldHeight)
        {
            return enumskyblock.defaultLightValue;
        }
        if (Block.useNeighborBrightness[getBlockId(i, j, k)])
        {
            int j1 = getSavedLightValue(enumskyblock, i, j + 1, k);
            int k1 = getSavedLightValue(enumskyblock, i + 1, j, k);
            int l1 = getSavedLightValue(enumskyblock, i - 1, j, k);
            int i2 = getSavedLightValue(enumskyblock, i, j, k + 1);
            int j2 = getSavedLightValue(enumskyblock, i, j, k - 1);
            if (k1 > j1)
            {
                j1 = k1;
            }
            if (l1 > j1)
            {
                j1 = l1;
            }
            if (i2 > j1)
            {
                j1 = i2;
            }
            if (j2 > j1)
            {
                j1 = j2;
            }
            return j1;
        }
        else
        {
            return getSavedLightValue(enumskyblock, i, j, k);
        }
    }
    
    @Override
    public void setLightValue(EnumSkyBlock enumskyblock, int i, int j, int k, int l)
    {
        if (enumskyblock == EnumSkyBlock.Sky)
        {
            if (!worldObj.provider.hasNoSky)
            {
                skylightMap.put(new CoordinateWrapper(i, j, k), l);
            }
        }
        else if (enumskyblock == EnumSkyBlock.Block)
        {
            blocklightMap.put(new CoordinateWrapper(i, j, k), l);
        }
        else
        {
            return;
        }
    }

    @Override
    public int getBlockLightValue_do(int par1, int par2, int par3, boolean par4)
    {
        if (par1 >= -30000000 && par3 >= -30000000 && par1 < 30000000 && par3 < 30000000)
        {
            if (par4)
            {
                int l = this.getBlockId(par1, par2, par3);

                if (Block.useNeighborBrightness[l])
                {
                    int i1 = this.getBlockLightValue_do(par1, par2 + 1, par3, false);
                    int j1 = this.getBlockLightValue_do(par1 + 1, par2, par3, false);
                    int k1 = this.getBlockLightValue_do(par1 - 1, par2, par3, false);
                    int l1 = this.getBlockLightValue_do(par1, par2, par3 + 1, false);
                    int i2 = this.getBlockLightValue_do(par1, par2, par3 - 1, false);

                    if (j1 > i1)
                    {
                        i1 = j1;
                    }

                    if (k1 > i1)
                    {
                        i1 = k1;
                    }

                    if (l1 > i1)
                    {
                        i1 = l1;
                    }

                    if (i2 > i1)
                    {
                        i1 = i2;
                    }

                    return i1;
                }
            }

            if (par2 < 0)
            {
                return 0;
            }
            else
            {
                if (par2 >= 256)
                {
                    par2 = 255;
                }

                Chunk chunk = this.getChunkFromChunkCoords(par1 >> 4, par3 >> 4);
                par1 &= 15;
                par3 &= 15;
                return chunk.getBlockLightValue(par1, par2, par3, this.skylightSubtracted);
            }
        }
        else
        {
            return 15;
        }
    }
    
    @Override
    public boolean canBlockSeeTheSky(int i, int j, int k)
    {
    	if (j < getHeightValue(i,k)) return false;
    	else return true;
//    	EntityBlock block = getBlock(i,j,k);
//    	if (block != null)
//    	{
//    		return worldObj.canBlockSeeTheSky((int)(block.posX), (int)(block.posY), (int)(block.posZ));
//    	}
//    	else
//    	{
//    		return false;
//    	}
    }

    @Override
    public int getFullBlockLightValue(int i, int j, int k)
    {
        return getBlockLightValue(i, j, k, 0);
    }
    
    private int getBlockLightValue(int i, int j, int k, int l)
    {
        int i1 = worldObj.provider.hasNoSky ? 0 : getSavedLightValue(EnumSkyBlock.Sky, i, j, k);
        i1 -= l;
        int j1 = getSavedLightValue(EnumSkyBlock.Block, i, j, k);
        if(j1 > i1)
        {
            i1 = j1;
        }
        return i1;
    }

    public void enableLightingUpdates()
    {
    	if (WorldGrid.dynamicLighting && lightUpdatesAllowed)
    	{
        	//Update every block
        	for (EntityBlock block : gridBlocks)
        	{
        		updateAllLightTypes(block.gridX, block.gridY, block.gridZ);
        	}
    	}
    	else if (!lightUpdatesAllowed)
    	{
	    	lightUpdatesAllowed = true;
	    	
	    	//Update every block
	    	for (EntityBlock block : gridBlocks)
	    	{
	    		updateAllLightTypes(block.gridX, block.gridY, block.gridZ);
	    	}
    	}
    }
    
    public void updateLightByType(EnumSkyBlock enumskyblock, int i, int j, int k)
    {
    	if (!lightUpdatesAllowed) return;
    	int jOffset = 0;

    	//Center all calculations around J = 64
		jOffset = j - 64;
		j = 64;
    	
        int l = 0;
        int i1 = 0;
        int j1 = getSavedLightValue(enumskyblock, i, j + jOffset, k);
        int l1 = 0;
        int j2 = j1;
        int blockID = getBlockId(i, j + jOffset, k);
        int lightOpacity = Block.lightOpacity[blockID];
        if (lightOpacity == 0)
        {
            lightOpacity = 1;
        }
        int k4 = 0;
        if (enumskyblock == EnumSkyBlock.Sky)
        {
            k4 = computeSkyLightValue(j2, i, j + jOffset, k, blockID, lightOpacity);
        }
        else
        {
            k4 = computeBlockLightValue(j2, i, j + jOffset, k, blockID, lightOpacity);
        }
        l1 = k4;
        if (l1 > j1)
        {
        	lightUpdateBlockList[i1++] = 0x20820;
        }
        else if (l1 < j1)
        {
            if (enumskyblock == EnumSkyBlock.Block);
            lightUpdateBlockList[i1++] = 0x20820 + (j1 << 18);
            do
            {
                if (l >= i1)
                {
                    break;
                }
                int k2 = lightUpdateBlockList[l++];
                int j3 = ((k2 & 0x3f) - 32) + i;
                int i4 = ((k2 >> 6 & 0x3f) - 32) + j;
                int l4 = ((k2 >> 12 & 0x3f) - 32) + k;
                int j5 = k2 >> 18 & 0xf;
                int l5 = getSavedLightValue(enumskyblock, j3, i4 + jOffset, l4);
                if (l5 == j5)
                {
                    setLightValue(enumskyblock, j3, i4 + jOffset, l4, 0);
                    if (j5 > 0)
                    {
                        int k6 = j3 - i;
                        int i7 = i4 - j;
                        int k7 = l4 - k;
                        if (k6 < 0)
                        {
                            k6 = -k6;
                        }
                        if (i7 < 0)
                        {
                            i7 = -i7;
                        }
                        if (k7 < 0)
                        {
                            k7 = -k7;
                        }
                        if (k6 + i7 + k7 < 17)
                        {
                            int i8 = 0;
                            while (i8 < 6)
                            {
                                int j8 = (i8 % 2) * 2 - 1;
                                int k8 = j3 + (((i8 / 2) % 3) / 2) * j8;
                                int l8 = i4 + (((i8 / 2 + 1) % 3) / 2) * j8;
                                int i9 = l4 + (((i8 / 2 + 2) % 3) / 2) * j8;
                                int i6 = getSavedLightValue(enumskyblock, k8, l8 + jOffset, i9);
                                int j9 = Block.lightOpacity[getBlockId(k8, l8 + jOffset, i9)];
                                if (j9 == 0)
                                {
                                    j9 = 1;
                                }
                                if (i6 == j5 - j9)
                                {
                                    lightUpdateBlockList[i1++] = (k8 - i) + 32 + ((l8 - j) + 32 << 6) + ((i9 - k) + 32 << 12) + (j5 - j9 << 18);
                                }
                                i8++;
                            }
                        }
                    }
                }
            }
            while (true);
            l = 0;
        }
        do
        {
            if (l >= i1)
            {
                break;
            }
            int k1 = lightUpdateBlockList[l++];
            int i2 = ((k1 & 0x3f) - 32) + i;
            int l2 = ((k1 >> 6 & 0x3f) - 32) + j;
            int k3 = ((k1 >> 12 & 0x3f) - 32) + k;
            int j4 = getSavedLightValue(enumskyblock, i2, l2 + jOffset, k3);
            int i5 = getBlockId(i2, l2 + jOffset, k3);
            int k5 = Block.lightOpacity[i5];
            if (k5 == 0)
            {
                k5 = 1;
            }
            int j6 = 0;
            if (enumskyblock == EnumSkyBlock.Sky)
            {
                j6 = computeSkyLightValue(j4, i2, l2 + jOffset, k3, i5, k5);
            }
            else
            {
                j6 = computeBlockLightValue(j4, i2, l2 + jOffset, k3, i5, k5);
            }
            if (j6 != j4)
            {
                setLightValue(enumskyblock, i2, l2 + jOffset, k3, j6);
                if (j6 > j4)
                {
                    int l6 = i2 - i;
                    int j7 = l2 - j;
                    int l7 = k3 - k;
                    if (l6 < 0)
                    {
                        l6 = -l6;
                    }
                    if (j7 < 0)
                    {
                        j7 = -j7;
                    }
                    if (l7 < 0)
                    {
                        l7 = -l7;
                    }
                    if (l6 + j7 + l7 < 17 && i1 < lightUpdateBlockList.length - 6)
                    {
                        if (getSavedLightValue(enumskyblock, i2 - 1, l2 + jOffset, k3) < j6)
                        {
                            lightUpdateBlockList[i1++] = (i2 - 1 - i) + 32 + ((l2 - j) + 32 << 6) + ((k3 - k) + 32 << 12);
                        }
                        if (getSavedLightValue(enumskyblock, i2 + 1, l2 + jOffset, k3) < j6)
                        {
                            lightUpdateBlockList[i1++] = ((i2 + 1) - i) + 32 + ((l2 - j) + 32 << 6) + ((k3 - k) + 32 << 12);
                        }
                        if (getSavedLightValue(enumskyblock, i2, l2 - 1 + jOffset, k3) < j6)
                        {
                            lightUpdateBlockList[i1++] = (i2 - i) + 32 + ((l2 - 1 - j) + 32 << 6) + ((k3 - k) + 32 << 12);
                        }
                        if (getSavedLightValue(enumskyblock, i2, l2 + 1 + jOffset, k3) < j6)
                        {
                            lightUpdateBlockList[i1++] = (i2 - i) + 32 + (((l2 + 1) - j) + 32 << 6) + ((k3 - k) + 32 << 12);
                        }
                        if (getSavedLightValue(enumskyblock, i2, l2 + jOffset, k3 - 1) < j6)
                        {
                            lightUpdateBlockList[i1++] = (i2 - i) + 32 + ((l2 - j) + 32 << 6) + ((k3 - 1 - k) + 32 << 12);
                        }
                        if (getSavedLightValue(enumskyblock, i2, l2 + jOffset, k3 + 1) < j6)
                        {
                            lightUpdateBlockList[i1++] = (i2 - i) + 32 + ((l2 - j) + 32 << 6) + (((k3 + 1) - k) + 32 << 12);
                        }
                    }
                }
            }
        }
        while (true);
    }
    

    private int computeSkyLightValue(int i5, int i, int j, int k, int i1, int j1)
    {
        int k1 = 0;
        if (canBlockSeeTheSky(i, j, k))
        {
            k1 = 15;
        }
        else
        {
            if (j1 == 0)
            {
                j1 = 1;
            }
            for (int l1 = 0; l1 < 6; l1++)
            {
                int i2 = (l1 % 2) * 2 - 1;
                int j2 = i + (((l1 / 2) % 3) / 2) * i2;
                int k2 = j + (((l1 / 2 + 1) % 3) / 2) * i2;
                int l2 = k + (((l1 / 2 + 2) % 3) / 2) * i2;
                int i3 = getSavedLightValue(EnumSkyBlock.Sky, j2, k2, l2) - j1;
                if (i3 > k1)
                {
                    k1 = i3;
                }
            }
        }
        return k1;
    }

    private int computeBlockLightValue(int i5, int i, int j, int k, int i1, int j1)
    {
        int k1 = Block.lightValue[i1];
        int l1 = getSavedLightValue(EnumSkyBlock.Block, i - 1, j, k) - j1;
        int i2 = getSavedLightValue(EnumSkyBlock.Block, i + 1, j, k) - j1;
        int j2 = getSavedLightValue(EnumSkyBlock.Block, i, j - 1, k) - j1;
        int k2 = getSavedLightValue(EnumSkyBlock.Block, i, j + 1, k) - j1;
        int l2 = getSavedLightValue(EnumSkyBlock.Block, i, j, k - 1) - j1;
        int i3 = getSavedLightValue(EnumSkyBlock.Block, i, j, k + 1) - j1;
        if (l1 > k1)
        {
            k1 = l1;
        }
        if (i2 > k1)
        {
            k1 = i2;
        }
        if (j2 > k1)
        {
            k1 = j2;
        }
        if (k2 > k1)
        {
            k1 = k2;
        }
        if (l2 > k1)
        {
            k1 = l2;
        }
        if (i3 > k1)
        {
            k1 = i3;
        }
        return k1;
    }
    
    //END LIGHTING
    
    

	@Override
	public int getBlockMetadata(int x, int y, int z) 
	{
		EntityBlock block = getBlock(x, y, z);
		if (block != null)
		{
			return block.meta;
		}
		else
		{		
			return 0;
		}
	}
	
	@Override
	public boolean setBlock(int x, int y, int z, int id) 
	{
		EntityBlock blockOld = getBlock(x, y, z);
		if (blockOld != null)
		{
			removeBlock(blockOld);
	        blockOld.setDead();
			Block.blocksList[blockOld.blockId].onBlockDestroyedByPlayer(this, x, y, z, blockOld.meta);
		}
		
		if (id != 0)
		{
			EntityBlock blockNew = ref.addChildBlock(id, x, y, z, 0);
			worldObj.spawnEntityInWorld(blockNew);
	        while (worldObj.loadedEntityList.remove(ref));
	        worldObj.spawnEntityInWorld(ref);
			Block.blocksList[id].onBlockAdded(this, x, y, z);
		}
		return true;
	}
	
	@Override
	public boolean setBlock(int x, int y, int z, int id, int meta, int flag) 
	{
		EntityBlock blockOld = getBlock(x, y, z);
		if (blockOld != null)
		{
			removeBlock(blockOld);
	        blockOld.setDead();
			Block.blocksList[blockOld.blockId].onBlockDestroyedByPlayer(this, x, y, z, blockOld.meta);
		}

		if (id != 0)
		{
			EntityBlock blockNew = ref.addChildBlock(id, x, y, z, meta);
			worldObj.spawnEntityInWorld(blockNew);
	        while (worldObj.loadedEntityList.remove(ref));
	        worldObj.loadedEntityList.add(ref);
			Block.blocksList[id].onBlockAdded(this, x, y, z);
		}
		return true;
	}
	
	@Override
	public void tick()
	{
		this.theProfiler.startSection("blockGrid-tick");
		tickUpdates(false);
		this.theProfiler.endSection();
	}

	@Override
	public boolean spawnEntityInWorld(Entity entity)
	{
		double origX = entity.posX - 0.5;
		double origZ = entity.posZ - 0.5;


    	double newX = ref.posX + (origX*Math.cos(-ref.rotationYaw/180*(Math.PI)) - origZ*Math.sin(-ref.rotationYaw/180*(Math.PI)));
    	double newY = ref.posY + entity.posY;
    	double newZ = ref.posZ + (origX*Math.sin(-ref.rotationYaw/180*(Math.PI)) + origZ*Math.cos(-ref.rotationYaw/180*(Math.PI)));

		entity.rotationYaw += ref.rotationYaw;
    	double motionX = (entity.motionX*Math.cos(-ref.rotationYaw/180*(Math.PI)) - entity.motionZ*Math.sin(-ref.rotationYaw/180*(Math.PI)));
    	double motionZ = (entity.motionX*Math.sin(-ref.rotationYaw/180*(Math.PI)) + entity.motionZ*Math.cos(-ref.rotationYaw/180*(Math.PI)));
    	entity.setPosition(newX, newY, newZ);
    	entity.setVelocity(motionX, entity.motionY, motionZ);
    	
    	if (entity instanceof EntityFireball)
    	{
    		//Special acceleration vectors for EntityFireball
    		double accelerationX = (((EntityFireball)entity).accelerationX*Math.cos(-ref.rotationYaw/180*(Math.PI)) - ((EntityFireball)entity).accelerationZ*Math.sin(-ref.rotationYaw/180*(Math.PI)));
        	double accelerationZ = (((EntityFireball)entity).accelerationX*Math.sin(-ref.rotationYaw/180*(Math.PI)) + ((EntityFireball)entity).accelerationZ*Math.cos(-ref.rotationYaw/180*(Math.PI)));
        	((EntityFireball)entity).accelerationX = accelerationX;
        	((EntityFireball)entity).accelerationZ = accelerationZ;
    	}
    	
    	if (entity.worldObj == this)
    	{
    		entity.worldObj = worldObj;
    	}
		
		return worldObj.spawnEntityInWorld(entity);
	}

	@Override
    public void spawnParticle(String s, double d, double d1, double d2, 
            double d3, double d4, double d5) {
    	double rotationYaw = ref.rotationYaw;    	
    	
    	double newX = ref.posX - 0.5 + (d*Math.cos(-rotationYaw/180*(Math.PI)) - d2*Math.sin(-rotationYaw/180*(Math.PI)));
    	double newY = ref.posY - 0.5 + d1;
    	double newZ = ref.posZ - 0.5 + (d*Math.sin(-rotationYaw/180*(Math.PI)) + d2*Math.cos(-rotationYaw/180*(Math.PI)));
    	
    	worldObj.spawnParticle(s, newX, newY, newZ, d3, d4, d5);
	}

	@Override
	public void playSoundEffect(double d, double d1, double d2, String s, float f, float f1) {
    	double rotationYaw = ref.rotationYaw;    	
    	
    	double newX = ref.posX - 0.5 + (d*Math.cos(-rotationYaw/180*(Math.PI)) - d2*Math.sin(-rotationYaw/180*(Math.PI)));
    	double newY = ref.posY - 0.5 + d1;
    	double newZ = ref.posZ - 0.5 + (d*Math.sin(-rotationYaw/180*(Math.PI)) + d2*Math.cos(-rotationYaw/180*(Math.PI)));
    	
    	worldObj.playSoundEffect(newX, newY, newZ, s, f, f1);
	}

	@Override
	public long getWorldTime() {
		return worldObj.getWorldTime();
	}

	@Override
	public WorldChunkManager getWorldChunkManager() 
	{
		return new FakeChunkManager();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List getEntitiesWithinAABBExcludingEntity(Entity entity, AxisAlignedBB axisalignedbb)
    {
		// TODO Auto-generated method stub
        return new ArrayList<Entity>();
    }
	
	@SuppressWarnings("rawtypes")
	@Override
    public List getEntitiesWithinAABB(Class class1, AxisAlignedBB axisalignedbb)
    {
		// TODO Auto-generated method stub
        return new ArrayList<Entity>();
    }
	
    @Override
    public void playAuxSFXAtEntity(EntityPlayer entityplayer, int i, int j, int k, int l, int i1)
    {
    	EntityBlock block = getBlock(j, k, l);
    	worldObj.playAuxSFXAtEntity(entityplayer, i, (int)block.posX, (int)block.posY, (int)block.posZ, i1);
    }
	
    @Override
	public boolean blockExists(int x, int y, int z)
	{
		//return true;
		//return (getBlock(i,j,k) != null);
    	return (y >= minGridY && x >= minGridX && z >= minGridZ && getBlockId(x, y, z) != 0);
	}

    @Override
    public boolean checkChunksExist(int i, int j, int k, int l, int i1, int j1)
    {
    	return true;
    }
    
    @Override
    public boolean isBlockNormalCubeDefault(int i, int j, int k, boolean flag)
    {
        Block block = Block.blocksList[getBlockId(i, j, k)];
        if(block == null)
        {
            return false;
        } else
        {
            return block.blockMaterial.isOpaque() && block.renderAsNormalBlock();
        }
    }
    
    public Chunk getChunkFromChunkCoords(int i, int j)
    {
        return new FakeChunk(this, i, j);
    }
    
    public boolean doChunksNearChunkExist(int i, int j, int k, int l)
    {
    	return true;
    }
	
    public BiomeGenBase getBiomeGenForCoords(int par1, int par2)
    {
    	return BiomeGenBase.forestHills;
    }

	/*
	 * Override methods we don't want this 'world' to be able to use.
	 */
	
	@Override
	public IChunkProvider getChunkProvider() { return new FakeChunkProvider(this); }
	
	//
	// End of World routines.
	//
	
	public void notifyBlocksCreation() {
		for (EntityBlock block : gridBlocks)
		{
			if (block.blockId == 0) {
				gridBlocks.remove(block);
			}
		}
//		for (EntityBlock block : gridBlocks)
//		{
//			Block.blocksList[block.blockId].onBlockAdded(this, block.gridX, block.gridY, block.gridZ);
//		}
		
		// Using an array because removeBlock() may be called
		
		EntityBlock gridBlocksArr[] = gridBlocks.toArray(new EntityBlock[0]);
		for (int i = 0; i < gridBlocksArr.length; i++)
		{
			EntityBlock block = gridBlocksArr[i];
			notifyBlocksOfNeighborChange(block.gridX, block.gridY, block.gridZ, block.blockId);
		}
	}
	
	@Override
	public String toString()
	{
		AxisAlignedBB box = getTotalBoundingBox();
		String s = "WorldGrid\n";
		s += "Size: " + (box.maxX - box.minX - 1) + "x" + (box.maxY - box.minY - 3) + "x" + (box.maxZ - box.minZ - 1) + "\n";
		s += "Blocks:";
		for (int y = (int)box.minY + 1; y <= box.maxY - 3; y++)
		{
			s += "\n";
			for (int z = (int)box.minZ + 1; z <= box.maxZ - 1; z++)
			{
				for (int x = (int)box.minX + 1; x <= box.maxX - 1; x++)
				{
					s += getBlockId(x, y, z) + "\t";
				}
				s += "\n";
			}
		}
		s += "Metadata:";
		for (int y = (int)box.minY + 1; y <= box.maxY - 3; y++)
		{
			s += "\n";
			for (int z = (int)box.minZ + 1; z <= box.maxZ - 1; z++)
			{
				for (int x = (int)box.minX + 1; x <= box.maxX - 1; x++)
				{
					s += getBlockMetadata(x, y, z) + "\t";
				}
				s += "\n";
			}
		}
		return s;
	}
}
