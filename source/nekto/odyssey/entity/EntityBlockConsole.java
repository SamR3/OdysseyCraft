package nekto.odyssey.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class EntityBlockConsole extends EntityBlock
{

	public EntityBlockConsole(World world)
	{
		super(world);
	}

	public EntityBlockConsole(World world, NBTTagCompound data)
	{
		super(world, data);
	}
	
	@Override
	protected void entityInit() {}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {}
	
	protected abstract EntityBlock newChildBlock();
	
	public EntityBlock addChildBlock(int id, int meta, int x, int y, int z)
	{
		if(this.getGrid() == null)
		{
			setDead();
			return null;
		}
			
		EntityBlock child = newChildBlock();
		child.blockId = id;
		child.meta = meta;
		child.setGrid(this.getGrid());
		
		child.setGridPosition(x, y, z);
		this.getGrid().addBlock(child);
		
		return child;
	}
	
	/*
	 * public ArrayList<EntityBlock> childBlocks = new ArrayList<EntityBlock>();
	 * 
	 * public int blockId; public int meta;
	 * 
	 * public EntityBlockConsole(World par1World, int x, int y, int z, int id,
	 * int meta) { super(par1World, x, y, z, id, meta); }
	 * 
	 * @Override public void onUpdate() { this.motionX *= 0.95; this.motionY *=
	 * 0.95; this.motionZ *= 0.95;
	 * 
	 * super.onUpdate();
	 * 
	 * //this.setAngles(this.rotationYaw, 0); }
	 * 
	 * public void setSpeed(float x, float y, float z, float yaw) { this.motionX
	 * += x; this.motionY += y; this.motionZ += z;
	 * 
	 * this.rotationYaw = MathHelper.wrapAngleTo180_float(yaw);
	 * 
	 * System.out.println("Set speed: " + this.motionX + ", " + this.motionY +
	 * ", " + this.motionZ); }
	 * 
	 * public double[] getSpeed() { double[] speeds = new double[]{
	 * this.motionX, this.motionY, this.motionZ };
	 * 
	 * return speeds; }
	 * 
	 * @Override public boolean interactFirst(EntityPlayer par1EntityPlayer) {
	 * int[] blockList = new int[] { 1 };
	 * 
	 * new Traverser((int) Math.round(posX - 0.5), (int) Math.round(posY - 0.5),
	 * (int) Math.round(posZ - 0.5), blockList, par1EntityPlayer, this.worldObj,
	 * this); this.rejoinWorld();
	 * 
	 * return true; }
	 * 
	 * public void addChild(EntityBlock ent) { this.childBlocks.add(ent); }
	 * 
	 * @Override public void setSpeedFromParent() {}
	 * 
	 * @Override protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
	 * { super.readEntityFromNBT(nbttagcompound); }
	 * 
	 * @Override protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
	 * { super.writeEntityToNBT(nbttagcompound); }
	 */
}
