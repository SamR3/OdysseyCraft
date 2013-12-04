package nekto.odyssey.world;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;

public class FakeWorldProvider extends WorldProvider
{

	@Override
    protected void generateLightBrightnessTable()
    {
        float f = 0.0F;
        for(int i = 0; i <= 15; i++)
        {
            float f1 = 1.0F - (float)i / 15F;
            lightBrightnessTable[i] = ((1.0F - f1) / (f1 * 3F + 1.0F)) * (1.0F - f) + f;
        }

    }

    @Override
    protected void registerWorldChunkManager()
    {
        //worldChunkMgr = new WorldChunkManager(worldObj);
    }

    @Override
    public boolean canCoordinateBeSpawn(int i, int j)
    {
        return true;
    }

    @Override
    public float calculateCelestialAngle(long l, float f)
    {
        int i = (int)(l % 24000L);
        float f1 = ((float)i + f) / 24000F - 0.25F;
        if(f1 < 0.0F)
        {
            f1++;
        }
        if(f1 > 1.0F)
        {
            f1--;
        }
        float f2 = f1;
        f1 = 1.0F - (float)((Math.cos((double)f1 * 3.1415926535897931D) + 1.0D) / 2D);
        f1 = f2 + (f1 - f2) / 3F;
        return f1;
    }

    @Override
    public float[] calcSunriseSunsetColors(float f, float f1)
    {
        return null;
    }

    @Override
    public Vec3 getFogColor(float f, float f1)
    {
        float f2 = MathHelper.cos(f * 3.141593F * 2.0F) * 2.0F + 0.5F;
        if(f2 < 0.0F)
        {
            f2 = 0.0F;
        }
        if(f2 > 1.0F)
        {
            f2 = 1.0F;
        }
        float f3 = 0.7529412F;
        float f4 = 0.8470588F;
        float f5 = 1.0F;
        f3 *= f2 * 0.94F + 0.06F;
        f4 *= f2 * 0.94F + 0.06F;
        f5 *= f2 * 0.91F + 0.09F;
        return Vec3.createVectorHelper(f3, f4, f5);
    }

    @Override
    public float getCloudHeight()
    {
        return worldObj.provider.getCloudHeight();
    }
    
    @Override
    public ChunkCoordinates getEntrancePortalLocation()
    {
        return null;
    }

	@Override
	public String getDimensionName() {
		// TODO Auto-generated method stub
		return null;
	}

}
