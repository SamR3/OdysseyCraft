package nekto.odyssey.world;

import java.util.List;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

public class FakeChunkProvider implements IChunkProvider
{

	private World worldObj;

	public FakeChunkProvider(World worldObj)
	{
		this.worldObj = worldObj;
	}

	@Override
	public boolean chunkExists(int i, int j)
	{
		return true;
	}

	@Override
	public Chunk provideChunk(int i, int j)
	{
		return new FakeChunk(worldObj, i, j);
	}

	@Override
	public Chunk loadChunk(int i, int j)
	{
		return new FakeChunk(worldObj, i, j);
	}

	@Override
	public void populate(IChunkProvider ichunkprovider, int i, int j)
	{
	}

	@Override
	public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate)
	{
		return false;
	}

	@Override
	public boolean canSave()
	{
		return false;
	}

	@Override
	public String makeString()
	{
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getPossibleCreatures(EnumCreatureType enumcreaturetype, int i,
			int j, int k)
	{
		// TODO Auto-generated method stub
		// Not sure what to do here
		return null;
	}

	@Override
	public ChunkPosition findClosestStructure(World world, String s, int i,
			int j, int k)
	{
		// TODO Auto-generated method stub
		// Not sure what to do here
		return null;
	}

	@Override
	public boolean unloadQueuedChunks()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getLoadedChunkCount()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void recreateStructures(int i, int j)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void saveExtraData()
	{
		// TODO Auto-generated method stub

	}

}
