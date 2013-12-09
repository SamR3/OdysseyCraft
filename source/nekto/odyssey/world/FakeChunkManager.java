package nekto.odyssey.world;

import java.util.List;
import java.util.Random;

import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

public class FakeChunkManager extends WorldChunkManager
{
	public FakeChunkManager()
	{

	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getBiomesToSpawnIn()
	{
		return null;
	}

	@Override
	public BiomeGenBase getBiomeGenAt(int i, int j)
	{
		return BiomeGenBase.forest;
	}

	@Override
	public float[] getRainfall(float af[], int i, int j, int k, int l)
	{
		return af;
	}

	@Override
	public float[] getTemperatures(float af[], int i, int j, int k, int l)
	{
		return null;
	}

	@Override
	public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase abiomegenbase[],
			int i, int j, int k, int l)
	{
		return BiomeGenBase.biomeList;
	}

	@Override
	public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase abiomegenbase[],
			int i, int j, int k, int l)
	{
		return BiomeGenBase.biomeList;
	}

	@Override
	public BiomeGenBase[] getBiomeGenAt(BiomeGenBase abiomegenbase[], int i,
			int j, int k, int l, boolean flag)
	{
		return BiomeGenBase.biomeList;
	}

	@Override
	public boolean areBiomesViable(int i, int j, int k,
			@SuppressWarnings("rawtypes") List list)
	{
		return false;
	}

	@Override
	public ChunkPosition findBiomePosition(int i, int j, int k,
			@SuppressWarnings("rawtypes") List list, Random random)
	{
		return null;
	}

	@Override
	public void cleanupCache()
	{
	}
}
