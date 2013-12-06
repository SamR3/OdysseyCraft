package nekto.odyssey.entity;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import nekto.odyssey.metarotation.BlockMetaPair;
import nekto.odyssey.metarotation.MetaRotation;
import nekto.odyssey.metarotation.RotatedBB;
import nekto.odyssey.world.WorldGrid;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.ModLoader;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityBlock extends Entity
{
	@SuppressWarnings("rawtypes")
	public static Hashtable<Integer, Class> entityClassLookup = new Hashtable<Integer, Class>(4);
	@SuppressWarnings("rawtypes")
	public static Hashtable<Class, Integer> entityClassReverseLookup = new Hashtable<Class, Integer>(4);
	
	public int gridX;
	public int gridY;
	public int gridZ;
	
	public int blockId;
	public int meta;
	
	private WorldGrid grid = null;
	
	static
	{
		entityClassLookup.put(0, EntityBlock.class);
		entityClassReverseLookup.put(EntityBlock.class, 0);
	}
	
	public EntityBlock(World world)
	{
		super(world);
		setSize(1.0F, 1.0F);
	}
	
	public EntityBlock(World world, NBTTagCompound data)
	{
		this(world);
		
		this.gridX = data.getInteger("gridX");
		this.gridY = data.getInteger("gridY");
		this.gridZ = data.getInteger("gridZ");
		
		this.blockId = data.getInteger("blockId");
		this.meta = data.getInteger("meta");
	}
	
	public void setGrid(WorldGrid newGrid)
	{
		this.grid = newGrid;
	}
	
	public WorldGrid getGrid()
	{
		return grid;
	}
	
	public void rejoinWorld(boolean notify)
	{
		int currentDirection = (Math.round(rotationYaw / 90) + 4) % 4;
		BlockMetaPair newBlock = MetaRotation.rotateMeta(blockId, meta, currentDirection);
		
		if (notify)
		{
			worldObj.setBlockMetadataWithNotify((int)Math.round(posX - 0.5), (int)Math.round(posY - 0.5), (int)Math.round(posZ - 0.5), newBlock.id, newBlock.meta);
		}
		else
		{
			worldObj.setBlock((int)Math.round(posX - 0.5), (int)Math.round(posY - 0.5), (int)Math.round(posZ - 0.5), newBlock.id, newBlock.meta, 2);
		}
		
		worldObj.markBlockForUpdate((int)Math.round(posX - 0.5), (int)Math.round(posY - 0.5), (int)Math.round(posZ - 0.5));
	
		setDead();
	}
	
	public void setGridPosition(int x, int y, int z)
	{
		gridX = x;
		gridY = y;
		gridZ = z;
	}
	
	public void updatePositionFromConsole()
	{
		prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        
		rotationYaw = getGrid().ref.rotationYaw;
		prevRotationYaw = getGrid().ref.prevRotationYaw;
		
		posX = getGrid().ref.posX + gridX * MathHelper.cos((float) (-getGrid().ref.rotationYaw/180*(Math.PI))) - gridZ*MathHelper.sin((float) (-getGrid().ref.rotationYaw/180*(Math.PI)));
		posY = getGrid().ref.posY + gridY;
		posZ = getGrid().ref.posZ + gridX * MathHelper.sin((float) (-getGrid().ref.rotationYaw/180*(Math.PI))) + gridZ*MathHelper.cos((float) (-getGrid().ref.rotationYaw/180*(Math.PI)));
		
		
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
				block.setBlockBoundsBasedOnState(getGrid(), gridX, gridY, gridZ);
				boundingBox.setBounds(
						posX + block.getBlockBoundsMinX() - 0.5, 
						posY + block.getBlockBoundsMinY() - 0.5, 
						posZ + block.getBlockBoundsMinZ() - 0.5, 
						posX + block.getBlockBoundsMaxX() - 0.5, 
						posY + block.getBlockBoundsMaxY() - 0.5, 
						posZ + block.getBlockBoundsMaxZ() - 0.5);
			}
			else
			{
				boundingBox.setBounds(posX - 0.5, posY - 0.5, posZ - 0.5, posX + 0.5, posY + 0.5, posZ + 0.5);
			}
		}
	}
	
	@Override
	public AxisAlignedBB getBoundingBox()
	{	
		if (isDead) return null;

		Block block = Block.blocksList[blockId];
		if (block != null)
		{
			block.setBlockBoundsBasedOnState(getGrid(), gridX, gridY, gridZ);
			AxisAlignedBB alignedBox = block.getCollisionBoundingBoxFromPool(getGrid(), gridX, gridY, gridZ);
			
			if (alignedBox != null) 
			{
				RotatedBB rotatedBox = RotatedBB.getBoundingBoxFromPool(alignedBox, 0);
				
				rotatedBox.offset(-0.5D, -0.5D, -0.5D);
				rotatedBox.rotateAroundVec3(Vec3.createVectorHelper(0.0D, 0.0D, 0.0D), rotationYaw);
				rotatedBox.offset(getGrid().ref.posX, getGrid().ref.posY, getGrid().ref.posZ);
				
				alignedBox.setBB(rotatedBox);
			
				return rotatedBox;
			}
		}
		
		return null;
	}
	
	@Override
	public AxisAlignedBB getCollisionBox(Entity entity)
	{
		return getBoundingBox();
	}
	
	@Override
	public boolean interactFirst(EntityPlayer entityplayer)
	{
		if (getGrid() == null) {
			setDead();
			return false;
		}
		
		//TODO: Fix
		if (Block.blocksList[blockId].onBlockActivated(grid, gridX, gridY, gridZ, entityplayer, meta, 0, 0, 0))
		{
			return true;
		}
		
		
		if (entityplayer == null || entityplayer.inventory == null)
		{
			return false;
		}
		
		ItemStack stack = entityplayer.inventory.getCurrentItem();

		if (stack != null && Item.itemsList[stack.itemID] != null)
		{
			Boolean ret = false;
			Item item = Item.itemsList[stack.itemID];
			Minecraft mc = ModLoader.getMinecraftInstance();
			Vec3 vec3d = mc.renderViewEntity.getPosition(1);
			Vec3 vec3d1 = mc.renderViewEntity.getLook(1);
			Vec3 vec3d2 = vec3d.addVector(vec3d1.xCoord * 10, vec3d1.yCoord * 10, vec3d1.zCoord * 10);
			
			RotatedBB box = RotatedBB.getBoundingBoxFromPool(boundingBox, rotationYaw);
			if (box != null)
			{
				MovingObjectPosition mop = box.calculateIntercept(vec3d, vec3d2);
	
				if (mop != null)
				{
					int side = mop.sideHit;
					
					//TODO: Fix
					if (item.onItemUse(stack, entityplayer, getGrid(), gridX, gridY, gridZ, side, 0, 0, 0))
					{
						entityplayer.swingItem();
						ret = true;
					}
					
					if(stack.stackSize == 0)
		            {
						entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = null;
		            }
					
					return ret;
				}
			}
		}	
		
		return false;
	}
	
	public NBTTagCompound saveChildBlock(NBTTagCompound childData)
	{
		int blockGridEntityClassID = EntityBlock.entityClassReverseLookup.get(this.getClass());
		childData.setInteger("BlockGridEntityClassID", blockGridEntityClassID);
		childData.setInteger("gridX", gridX);
		childData.setInteger("gridY", gridY);
		childData.setInteger("gridZ", gridZ);
		childData.setInteger("blockId", blockId);
		childData.setInteger("blockMeta", meta);
		return childData;
	}
	
	public static EntityBlock createChildBlockFromNBT(NBTTagCompound childData)
	{
		int entityID = childData.getInteger("BlockGridEntityClassID");
		@SuppressWarnings("rawtypes")
		Class entityClass = EntityBlock.entityClassLookup.get(entityID);
		try {
			@SuppressWarnings("unchecked")
			EntityBlock entity = (EntityBlock)(entityClass.getConstructor(World.class).newInstance(ModLoader.getMinecraftInstance().theWorld));
			entity.loadChildBlock(childData);
			return entity;
		} catch (InstantiationException e) {
			System.out.printf("Unhandled InstantiationException for BlockGridEntity ID %d\n", entityID);
			return null;
		} catch (IllegalAccessException e) {
			System.out.printf("Unhandled IllegalAccessException for BlockGridEntity ID %d\n", entityID);
			return null;
		} catch (IllegalArgumentException e) {
			System.out.printf("Unhandled IllegalArgumentException for BlockGridEntity ID %d\n", entityID);
			return null;
		} catch (SecurityException e) {
			System.out.printf("Unhandled SecurityException for BlockGridEntity ID %d\n", entityID);
			return null;
		} catch (InvocationTargetException e) {
			System.out.printf("Unhandled InvocationTargetException for BlockGridEntity ID %d\n", entityID);
			return null;
		} catch (NoSuchMethodException e) {
			System.out.printf("Unhandled NoSuchMethodException for BlockGridEntity ID %d\n", entityID);
			return null;
		}
	}
	
	public void loadChildBlock(NBTTagCompound childData) 
	{
		this.gridX = childData.getInteger("gridX");
		this.gridY = childData.getInteger("gridY");
		this.gridZ = childData.getInteger("gridZ");
		this.blockId = childData.getInteger("blockId");
		this.meta = childData.getInteger("blockMeta");	
	}
	
	public boolean equals(EntityBlock test)
	{
		return (test.gridX == this.gridX &&
				test.gridY == this.gridY &&
				test.gridZ == this.gridZ);
	}
	
	@Override
	public void setPosition(double d, double d1, double d2) 
	{	
		if (this instanceof EntityBlockConsole) 
		{
			super.setPosition(d, d1, d2);
			//updateBoundingBox();
			if (getGrid() != null)
			{
				getGrid().updateChildPositions();
			}
		} else {
			//Empty so only the parent block can set position/rotation
		}
	}
	
	@Override
	protected void setRotation(float f, float f1)
	{
		if (this instanceof EntityBlockConsole) {
			super.setRotation(f, f1);
			if (getGrid() != null)
			{
				getGrid().updateChildPositions();
			}
		} else {
			//Empty so only the parent block can set position/rotation
		}
	}
	
	
	@Override
	public void setAngles(float f, float f1)
	{
		if (this instanceof EntityBlockConsole) {
			super.setAngles(f, f1);
			if (getGrid() != null)
			{
				getGrid().updateChildPositions();
			}
		} else {
			//Empty so only the parent block can set position/rotation
		}
	}
	
	@Override
	public void setPositionAndRotation(double d, double d1, double d2, float f, 
			float f1)
	{
		if (this instanceof EntityBlockConsole) 
		{
			super.setPositionAndRotation(d, d1, d2, f, f1);
			if (getGrid() != null)
			{
				getGrid().updateChildPositions();
			}
		} else {
			//Empty so only the parent block can set position/rotation
		}
	}

	@Override
	public void setLocationAndAngles(double d, double d1, double d2, float f, 
			float f1)
	{
		if (this instanceof EntityBlockConsole) 
		{
			super.setLocationAndAngles(d, d1, d2, f, f1);
			
			if (getGrid() != null)
			{
				getGrid().updateChildPositions();
			}
		} else {
			//Empty so only the parent block can set position/rotation
		}
	}
	
	@Override
	public boolean canBePushed()
	{
		return false;
	}
	
	@Override
	public boolean canBeCollidedWith()
	{
		if (blockId == Block.dispenser.blockID) return false; 
		return true; //BlockGrid.isBlockCollidable(blockId);
	}
	
	@Override
	public boolean handleWaterMovement()
	{
		return false;
	}
	
	@Override
	public float getCollisionBorderSize()
	{
		return 0;
	}
	
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

