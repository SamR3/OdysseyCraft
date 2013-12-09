package nekto.odyssey.craft;

import nekto.odyssey.entity.EntityBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class CraftEntity extends EntityBlock
{
	private int craftID;
	
	public int currentDamage = 0;
	private int timeSinceHit = 0;
	private int damageTicker = 0;
	public static int damageRepairDelay = 0;

	public CraftEntity(World world)
	{
		super(world);
	}
	
	public CraftEntity(World world, NBTTagCompound childData)
	{
		super(world, childData);
	}
	
	public void destroy(Boolean dropItem)
	{
		if (isDead)
		{
			return;
		}
		
		if (dropItem)
		{
			Block block = Block.blocksList[blockId];

			block.dropBlockAsItem(getGrid(), gridX, gridY, gridZ, meta, 0);
		}
		isDead = true;
		getGrid().setBlock(gridX, gridY, gridZ, 0);
	}
	
	//@Override
	public boolean attackEntityFrom(DamageSource damagesource, int i)
    {
		currentDamage += i;
		timeSinceHit = 10;
		if (currentDamage >= Block.blocksList[blockId].getBlockHardness(null, 0, 0, 0) * 4 + 1)
		{
			destroy(true);
		}
        setBeenAttacked();
        return true;
    }
    
	@Override
    public void applyEntityCollision(Entity entity)
    {
    }
    
    @Override
    public void moveEntity(double deltaX, double deltaY, double deltaZ)
    {
    	//This entity does not move directly
    }
    
	@Override
	protected void entityInit() {
	}

	
	@Override
	public void onUpdate()
    {
		//onEntityUpdate();
		if(timeSinceHit > 0)
        {
            timeSinceHit--;
        }
		if(timeSinceHit == 0 && currentDamage > 0)
        {
			damageTicker--;
			if (damageTicker <= 0)
			{
				currentDamage--;
				damageTicker = damageRepairDelay;
			}
        }
		else
		{
			damageTicker = damageRepairDelay;
		}
    }

	@Override
	public void onCollideWithPlayer(EntityPlayer entityPlayer)
	{

	}	
	
	public boolean equals(CraftEntity test)
	{
		return (test.craftID == this.craftID &&
				test.gridX == this.gridX &&
				test.gridY == this.gridY &&
				test.gridZ == this.gridZ);
	}
	
	
	public int getCraftID() {
		return craftID;
	}

	public void setCraftID(int craftID) {
		this.craftID = craftID;
	}

	@Override
	public NBTTagCompound saveChildBlock(NBTTagCompound childData)
	{
		return super.saveChildBlock(childData);
	}
	
	@Override
	public void loadChildBlock(NBTTagCompound childData) 
	{
		super.loadChildBlock(childData);
	}
	
	
	
	

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {}
}
