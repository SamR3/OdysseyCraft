package nekto.odyssey.entity;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class EntityBlock extends Entity
{
	public static Hashtable<Integer, Class> entityClassLookup = new Hashtable<INteger>
	
	
	
	
	
	/*public int blockId;
	public int meta;
	
	public EntityBlockConsole parent;
	
	public EntityBlock(World par1World, int x, int y, int z, int id, int meta)
	{
		super(par1World);

		setSize(1.0F, 1.0F);
		entityCollisionReduction = 1;
		this.yOffset = (this.height / 2);
		this.setLocationAndAngles(x + 0.5, y, z + 0.5, 0, 0);
		this.blockId = id;
		this.meta = meta;
		
		this.parent = null;
	}

	public EntityBlock(World par1World, int x, int y, int z, int id, int meta, EntityBlockConsole console)
	{
		super(par1World);

		setSize(1.0F, 1.0F);
		entityCollisionReduction = 1;
		this.yOffset = (this.height / 2);
		this.setLocationAndAngles(x + 0.5, y, z + 0.5, 0, 0);
		this.blockId = id;
		this.meta = meta;
		
		this.parent = console;
		
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
	}
	
	@Override
	public boolean canBePushed()
	{
		return true;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return !this.isDead;
	}

	@Override
	public void onUpdate()
	{
		this.setSpeedFromParent();		
		super.onUpdate();
		
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
	}
	
	@Override
	protected void entityInit()
	{
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		
		this.blockId = nbttagcompound.getInteger("blockId");
		this.meta = nbttagcompound.getInteger("meta");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBTOptional(nbttagcompound);
		
		nbttagcompound.setInteger("blockId", this.blockId);
		nbttagcompound.setInteger("meta", this.meta);
	}

	@Override
	public boolean interactFirst(EntityPlayer par1EntityPlayer)
	{		
		return false;
	}
	
	public void setSpeedFromParent()
	{
		double[] speeds = this.parent.getSpeed();
		
		this.motionX = speeds[0];
		this.motionY = speeds[1];
		this.motionZ = speeds[2];
	}

	public void rejoinWorld()
	{
		worldObj.setBlock((int) Math.round(posX - 0.5),
				(int) Math.round(posY - 0.5), (int) Math.round(posZ - 0.5),
				this.blockId, this.meta, 2);

		this.setDead();
	}

	public AxisAlignedBB getCollisionBox(Entity par1Entity)
    {
		if(this.isDead)
		{
			return null;
		}
		
        return par1Entity.boundingBox;
    }
	
	public AxisAlignedBB getBoundingBox()
    {
		if(this.isDead)
		{
			return null;
		}
	
        return this.boundingBox;
    }*/
	
}

