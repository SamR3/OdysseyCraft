package nekto.odyssey.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class EntityBlock extends Entity
{
	public int blockId;
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
		
		AxisAlignedBB bb = getBoundingBox();
		if (bb != null)
		{
			//bb.contract((bb.maxX - bb.minX)/2*Math.cos(rotationYaw%90), 0, (bb.maxZ - bb.minZ)/2*Math.cos(rotationYaw%90));
			boundingBox.setBB(bb);
		}
		else
		{
			Block block = Block.blocksList[blockId];
			if (block != null)
			{
				block.setBlockBoundsBasedOnState(this.worldObj, this.posX, this.posY, this.posZ);
				boundingBox.setBounds(
						posX + block.minX - 0.5, 
						posY + block.minY - 0.5, 
						posZ + block.minZ - 0.5, 
						posX + block.maxX - 0.5, 
						posY + block.maxY - 0.5, 
						posZ + block.maxZ - 0.5);
			}
			else
			{
				boundingBox.setBounds(posX - 0.5, posY - 0.5, posZ - 0.5, posX + 0.5, posY + 0.5, posZ + 0.5);
			}
		}
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
    }
	
	/**
     * Applies a velocity to each of the entities pushing them away from each other. Args: entity
     */
    /*public void applyEntityCollision(Entity par1Entity)
    {
        MinecraftForge.EVENT_BUS.post(new MinecartCollisionEvent(this, par1Entity));
        if (getCollisionHandler() != null)
        {
            getCollisionHandler().onEntityCollision(this, par1Entity);
            return;
        }
        if (!this.worldObj.isRemote)
        {
            if (par1Entity != this.riddenByEntity)
            {
                if (par1Entity instanceof EntityLivingBase && !(par1Entity instanceof EntityPlayer) && !(par1Entity instanceof EntityIronGolem) && canBeRidden()               && this.motionX * this.motionX + this.motionZ * this.motionZ > 0.01D && this.riddenByEntity == null && par1Entity.ridingEntity == null)
                {
                    par1Entity.mountEntity(this);
                }

                double d0 = par1Entity.posX - this.posX;
                double d1 = par1Entity.posZ - this.posZ;
                double d2 = d0 * d0 + d1 * d1;

                if (d2 >= 9.999999747378752E-5D)
                {
                    d2 = (double)MathHelper.sqrt_double(d2);
                    d0 /= d2;
                    d1 /= d2;
                    double d3 = 1.0D / d2;

                    if (d3 > 1.0D)
                    {
                        d3 = 1.0D;
                    }

                    d0 *= d3;
                    d1 *= d3;
                    d0 *= 0.10000000149011612D;
                    d1 *= 0.10000000149011612D;
                    d0 *= (double)(1.0F - this.entityCollisionReduction);
                    d1 *= (double)(1.0F - this.entityCollisionReduction);
                    d0 *= 0.5D;
                    d1 *= 0.5D;

                    if (par1Entity instanceof EntityMinecart)
                    {
                        double d4 = par1Entity.posX - this.posX;
                        double d5 = par1Entity.posZ - this.posZ;
                        Vec3 vec3 = this.worldObj.getWorldVec3Pool().getVecFromPool(d4, 0.0D, d5).normalize();
                        Vec3 vec31 = this.worldObj.getWorldVec3Pool().getVecFromPool((double)MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F), 0.0D, (double)MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F)).normalize();
                        double d6 = Math.abs(vec3.dotProduct(vec31));

                        if (d6 < 0.800000011920929D)
                        {
                            return;
                        }

                        double d7 = par1Entity.motionX + this.motionX;
                        double d8 = par1Entity.motionZ + this.motionZ;

                        if (((EntityMinecart)par1Entity).isPoweredCart() && !isPoweredCart())
                        {
                            this.motionX *= 0.20000000298023224D;
                            this.motionZ *= 0.20000000298023224D;
                            this.addVelocity(par1Entity.motionX - d0, 0.0D, par1Entity.motionZ - d1);
                            par1Entity.motionX *= 0.949999988079071D;
                            par1Entity.motionZ *= 0.949999988079071D;
                        }
                        else if (!((EntityMinecart)par1Entity).isPoweredCart() && isPoweredCart())
                        {
                            par1Entity.motionX *= 0.20000000298023224D;
                            par1Entity.motionZ *= 0.20000000298023224D;
                            par1Entity.addVelocity(this.motionX + d0, 0.0D, this.motionZ + d1);
                            this.motionX *= 0.949999988079071D;
                            this.motionZ *= 0.949999988079071D;
                        }
                        else
                        {
                            d7 /= 2.0D;
                            d8 /= 2.0D;
                            this.motionX *= 0.20000000298023224D;
                            this.motionZ *= 0.20000000298023224D;
                            this.addVelocity(d7 - d0, 0.0D, d8 - d1);
                            par1Entity.motionX *= 0.20000000298023224D;
                            par1Entity.motionZ *= 0.20000000298023224D;
                            par1Entity.addVelocity(d7 + d0, 0.0D, d8 + d1);
                        }
                    }
                    else
                    {
                        this.addVelocity(-d0, 0.0D, -d1);
                        par1Entity.addVelocity(d0 / 4.0D, 0.0D, d1 / 4.0D);
                    }
                }
            }
        }
    }*/
}

