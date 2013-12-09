package nekto.odyssey.world;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

public class FakeChunk extends Chunk
{
	public FakeChunk(World world, int i, int j)
	{
		super(world, i, j);
	}

	public FakeChunk(World world, byte abyte0[], int i, int j)
	{
		this(world, i, j);
	}

	@Override
	public boolean isAtLocation(int i, int j)
	{
		return i == xPosition && j == zPosition;
	}

	@Override
	public int getBlockID(int i, int j, int k)
	{
		return worldObj.getBlockId(i, j, k);
	}

	@Override
	public boolean setBlockIDWithMetadata(int i, int j, int k, int l, int i1)
	{
		return worldObj.setBlock(i, j, k, l, i1, 2);
	}

	@Override
	public int getBlockMetadata(int i, int j, int k)
	{
		return worldObj.getBlockMetadata(i, j, k);
	}

	@Override
	public boolean setBlockMetadata(int i, int j, int k, int l)
	{
		return worldObj.setBlock(i, j, k, l);
	}

	@Override
	public int getSavedLightValue(EnumSkyBlock enumskyblock, int i, int j, int k)
	{
		return ((WorldGrid) (worldObj)).getSavedLightValue(enumskyblock, i, j,
				k);
	}

	@Override
	public void setLightValue(EnumSkyBlock enumskyblock, int i, int j, int k,
			int l)
	{
		((WorldGrid) (worldObj)).setLightValue(enumskyblock, i, j, k, l);
	}

	@Override
	public int getBlockLightValue(int i, int j, int k, int l)
	{
		int i1 = worldObj.provider.hasNoSky ? 0 : ((WorldGrid) (worldObj))
				.getSavedLightValue(EnumSkyBlock.Sky, i, j, k);
		if (i1 > 0)
		{
			isLit = true;
		}
		i1 -= l;
		int j1 = ((WorldGrid) (worldObj)).getSavedLightValue(
				EnumSkyBlock.Block, i, j, k);
		if (j1 > i1)
		{
			i1 = j1;
		}
		return i1;
	}

	@Override
	public void addEntity(Entity entity)
	{
	}

	@Override
	public void removeEntity(Entity entity)
	{
	}

	@Override
	public void removeEntityAtIndex(Entity entity, int i)
	{
	}

	@Override
	public boolean canBlockSeeTheSky(int i, int j, int k)
	{
		return j >= (heightMap[k << 4 | i] & 0xff);
	}

	@Override
	public TileEntity getChunkBlockTileEntity(int i, int j, int k)
	{
		return worldObj.getBlockTileEntity(i, j, k);
	}

	public void addTileEntity(TileEntity tileentity)
	{
		// Fix
	}

	@Override
	public void setChunkBlockTileEntity(int i, int j, int k,
			TileEntity tileentity)
	{
		worldObj.setBlockTileEntity(i, j, k, tileentity);
	}

	@Override
	public void removeChunkBlockTileEntity(int i, int j, int k)
	{
		worldObj.removeBlockTileEntity(i, j, k);
	}

	@Override
	public void onChunkLoad()
	{
	}

	@Override
	public void onChunkUnload()
	{
	}

	@Override
	public void setChunkModified()
	{
	}

	@Override
	public boolean needsSaving(boolean flag)
	{
		return false;
	}

	@Override
	public Random getRandomWithSeed(long l)
	{
		return new Random(worldObj.getSeed()
				+ (long) (xPosition * xPosition * 0x4c1906)
				+ (long) (xPosition * 0x5ac0db)
				+ (long) (zPosition * zPosition) * 0x4307a7L
				+ (long) (zPosition * 0x5f24f) ^ l);
	}

	@Override
	public void populateChunk(IChunkProvider ichunkprovider,
			IChunkProvider ichunkprovider1, int i, int j)
	{
	}

	@Override
	public void updateSkylight()
	{
	}
}
