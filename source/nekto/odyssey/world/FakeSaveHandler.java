package nekto.odyssey.world;

import java.io.File;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

public class FakeSaveHandler implements ISaveHandler
{

	@Override
	public WorldInfo loadWorldInfo()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkSessionLock() throws MinecraftException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public IChunkLoader getChunkLoader(WorldProvider worldprovider)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveWorldInfoWithPlayer(WorldInfo worldinfo,
			NBTTagCompound nbttagcompound)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void saveWorldInfo(WorldInfo worldinfo)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public IPlayerFileData getSaveHandler()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public File getMapFileFromName(String s)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWorldDirectoryName()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
