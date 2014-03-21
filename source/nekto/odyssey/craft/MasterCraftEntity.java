package nekto.odyssey.craft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import nekto.odyssey.entity.EntityBlock;
import nekto.odyssey.entity.EntityBlockConsole;
import nekto.odyssey.metarotation.RotatedBB;
import nekto.odyssey.world.WorldGrid;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class MasterCraftEntity extends EntityBlockConsole
{
	private static HashMap<Integer, Integer> maxChunkHeightCache = new HashMap<Integer, Integer>();
	public static int entityCacheTime;
	
	private double forwardSpeed = 0;
	private double motionYaw = 0;
	private double ascent = 0;
	
	public double initialRotation = 0;
	
	private int craftID;
	
	public MasterCraftEntity(World world) {
		super(world);
	}
	
	public void setCraftID(int craftID) {
		this.craftID = craftID;
	}

	public int getCraftID() {
		return craftID;
	}
	
	public boolean wasCreatedWithRedstone;
	
	@Override
	protected EntityBlock newChildBlock()
	{
		return new CraftEntity(worldObj);
	}

	@Override
	public CraftEntity addChildBlock(int id, int x, int y, int z, int meta)
	{
		CraftEntity child = (CraftEntity) super.addChildBlock(id, x, y, z, meta);
		child.setCraftID(this.getCraftID());
		
		worldObj.spawnEntityInWorld(child);
		
		return child;
	}

	/*
	 * Movement and collision code
	 */
	
	public void destroy(Boolean dropItem)
	{
		stop();
		align();
		
		
		//Destroy all the children first
		if (!getGrid().gridBlocks.isEmpty())
		{
			getGrid().clear();
		}
		
		setDead();
		
//		
//		//Try to unload this entity.
//		List<Entity> tmpList = new ArrayList<Entity>();
//		//TERRIBLE HACK JOB
//		for (int i = 0; i < 40; i++) tmpList.add(this);
//		worldObj.unloadEntities(tmpList); //unloadEntities()
		
//		//Slightly less terrible hack
//		Set<Entity> s = new LinkedHashSet<Entity>(worldObj.loadedEntityList);
//		worldObj.loadedEntityList = new ArrayList<Entity>(s);
		

		
		//This checks all crafts for destroyed status, and cleans them up
		CraftManager.getInstance().craftDestroyed();
		
		if (dropItem)
		{
			EntityItem entityitem = new EntityItem(worldObj, posX, posY, posZ, new ItemStack(blockId, 1, meta));
			entityitem.delayBeforeCanPickup = 10;
			worldObj.spawnEntityInWorld(entityitem);
		}
		else
		{
			//rejoinWorld(true);
		}
	}
	
	private boolean childrenJoined = false;
	@SuppressWarnings("unchecked")
	@Override
	public void onUpdate()
	{
		this.worldObj.theProfiler.startSection("zeppelin-onUpdate");
		int joinCounter = 0;
		
		//Clear the chunk height cache
		maxChunkHeightCache = new HashMap<Integer, Integer>();
		
		//If we have children that have not yet joined the world, try to join them
		this.worldObj.theProfiler.startSection("childJoiner");
		if (childrenJoined == false)
		{
			for (Iterator<EntityBlock> childIterator = getGrid().gridBlocks.iterator(); childIterator.hasNext();) {
				EntityBlock child = childIterator.next();
				if (child.addedToChunk == false)
				{
					worldObj.spawnEntityInWorld(child);
					joinCounter++;
				}
			}
			//Put this entity at the end of the list (after the children is the important part)
			while (worldObj.loadedEntityList.remove(this));
			worldObj.loadedEntityList.add(this);
			//worldObj.entityJoinedWorld(this);
		}
		if (joinCounter == 0) childrenJoined = true;
		this.worldObj.theProfiler.endSection();
		
		this.worldObj.theProfiler.startSection("lighting");
		getGrid().enableLightingUpdates();
		this.worldObj.theProfiler.endSection();
		
		this.worldObj.theProfiler.startSection("entityUpdate");
		onEntityUpdate();
		this.worldObj.theProfiler.endSection();
		
		calculateMotionFromSpeedAndRotation();
		
		if (Math.abs(motionYaw) > 0.0001 || Math.abs(motionX) >= 0.001 || Math.abs(motionY) >= 0.001 || Math.abs(motionZ) >= 0.001)
		{
			//moveEntity();
			moveEntityOptimized();
		}

		//getGrid().tick();
		this.worldObj.theProfiler.endSection();
	}

	@Override
	public void moveEntity(double deltaX, double deltaY, double deltaZ)
	{
		
	}
	
	public boolean moveEntity()
	{
		double deltaX = motionX;
		double deltaY = motionY;
		double deltaZ = motionZ;
		this.worldObj.theProfiler.startSection("zeppelin-moveEntity");
		float deltaYaw = (float) motionYaw;
		ArrayList<AxisAlignedBB> boundingBoxes = new ArrayList<AxisAlignedBB>();
		ArrayList<ArrayList<AxisAlignedBB>> collidingBoxes = new ArrayList<ArrayList<AxisAlignedBB>>();

		calculateMotionFromSpeedAndRotation();
		
		ySize *= 0.4F;
		double origDeltaX = deltaX;
		double origDeltaY = deltaY;
		double origDeltaZ = deltaZ;
		

		AxisAlignedBB totalBox = getRotatedTotalBoundingBox();
		//First, check to see if we hit the ceiling or floor of the world
		//TODO Test if this works with height mods. Theoretically it should work with both normal and cubic chunks.
		if (!worldObj.blockExists((int)posX, (int)(totalBox.maxY + deltaY), (int)posZ) && deltaY > 0)
		{
			deltaY = 0;
		}
		if (!worldObj.blockExists((int)posX, (int)(totalBox.minY + deltaY), (int)posZ) && deltaY < 0)
		{
			deltaY = 0;
		}

		int maxHeightValue = 0;
		totalBox = totalBox.expand(3, 3, 3);
		for (int i = MathHelper.floor_double(totalBox.minX); i < MathHelper.floor_double(totalBox.maxX + 1.0D); i++)
		{
			for (int j = MathHelper.floor_double(totalBox.minZ); j < MathHelper.floor_double(totalBox.maxZ + 1.0D); j++)
			{
				if (worldObj.getHeightValue(i, j) > maxHeightValue)
				{
					maxHeightValue = worldObj.getHeightValue(i, j);
				}
			}
		}

		if (maxHeightValue > totalBox.minY)
		{
			//Giant collision code
			
			//Two arrays are used.  One to hold the bounding box, and a sibling array to hold the boxes it collided with.
			//Keys match, but are not meaningful (except 0 which is this master entity)
			
		 
			//Get the colliding bounding boxes, for the entire craft, excluding players
			//Reset the arrays
			boundingBoxes = new ArrayList<AxisAlignedBB>();
			collidingBoxes = new ArrayList<ArrayList<AxisAlignedBB>>();
	
			if (!getGrid().collidableGridBlocks.isEmpty())
			{
				for (EntityBlock child : getGrid().collidableGridBlocks)
				{
					if (child.getBoundingBox() == null) continue;

					ArrayList<AxisAlignedBB> childBoxes = this.getCollidingBoundingBoxesExcludingPlayers(worldObj, child, child.getBoundingBox().addCoord(deltaX, deltaY, deltaZ));
					if (childBoxes.size() > 0) // dont add it if theres nothing in it
					{
						deltaYaw = 0;
						boundingBoxes.add(child.getBoundingBox());
						collidingBoxes.add(childBoxes);
					}
				}
			}
	
			for(int childIndex = 0; childIndex < boundingBoxes.size(); childIndex++)
			{
				AxisAlignedBB box = boundingBoxes.get(childIndex);
				ArrayList<AxisAlignedBB> collidingBox = collidingBoxes.get(childIndex);
				
				for (int collidingBoxIndex = 0; collidingBoxIndex < collidingBox.size(); collidingBoxIndex++)
				{
					if (deltaX != 0) deltaX = (collidingBox.get(collidingBoxIndex)).calculateXOffset(box, deltaX);
					if (deltaY != 0) deltaY = (collidingBox.get(collidingBoxIndex)).calculateYOffset(box, deltaY);
					if (deltaZ != 0) deltaZ = (collidingBox.get(collidingBoxIndex)).calculateZOffset(box, deltaZ);
				}
				if (deltaX == 0 && deltaY == 0 && deltaZ == 0)
				{
					break;
				}
			}
		}
		
		rotationYaw += deltaYaw;
		rotationYaw = (rotationYaw + 360) % 360;
		prevRotationYaw = (float) (rotationYaw - deltaYaw);
		
		setPosition(posX + deltaX, posY + deltaY, posZ + deltaZ);
		
		isCollidedHorizontally = origDeltaX != deltaX || origDeltaZ != deltaZ;
		isCollidedVertically = origDeltaY != deltaY;
		onGround = origDeltaY != deltaY && origDeltaY < 0.0D;
		isCollided = isCollidedHorizontally || isCollidedVertically;
		//updateFallState(deltaY, onGround);
		if(origDeltaX != deltaX)
		{
			forwardSpeed = 0.0D;
			motionX = 0.0D;
		}
		if(origDeltaY != deltaY)
		{
			ascent = 0;
			motionY = 0.0D;
		}
		if(origDeltaZ != deltaZ)
		{
			forwardSpeed = 0.0D;
			motionZ = 0.0D;
		}
		

		
		
		if (deltaX != 0 || deltaY != 0 || deltaZ != 0 || deltaYaw != 0)
		{

			getGrid().updateChildPositions();
			
			//Just move every entity on the ship that isn't another craft entity
			ArrayList<Entity> collidingEntities = getEntitiesOnCraft();
			for (Entity thing : collidingEntities)
			{
				if (thing instanceof EntityBlock)
				{
					//Do nothing
				}
				else
				{
					double centerX = this.posX - deltaX;
					double centerZ = this.posZ - deltaZ;
					
					double thingDeltaX = (thing.posX - centerX);
					double thingDeltaZ = (thing.posZ - centerZ);
					
					double thingRotatedPosX = ((thingDeltaX*Math.cos((float) (-deltaYaw/180*(Math.PI))) - thingDeltaZ*Math.sin((float) (-deltaYaw/180*(Math.PI)))) + centerX);
					double thingRotatedPosZ = ((thingDeltaX*Math.sin((float) (-deltaYaw/180*(Math.PI))) + thingDeltaZ*Math.cos((float) (-deltaYaw/180*(Math.PI)))) + centerZ);
					
					thing.rotationYaw -= deltaYaw;
					if (deltaY > 0){
						thing.posY += deltaY;
						thing.boundingBox.offset(0, deltaY, 0);
					}
					thing.boundingBox.offset(deltaX, 0, deltaZ);
					
					//thing.moveEntity(thingRotatedPosX - thing.posX + deltaX , thing.motionY, thingRotatedPosZ - thing.posZ + deltaZ);
					thing.moveEntity(thingRotatedPosX - thing.posX , thing.motionY, thingRotatedPosZ - thing.posZ);

				}
			}
			this.worldObj.theProfiler.endSection();
			return true;
		}
		else
		{
			this.worldObj.theProfiler.endSection();
			return false;
		}
		
	}


	private int getMaximumChunkHeight(int chunkI, int chunkK)
	{
		Integer chunkHeight = maxChunkHeightCache.get((chunkI << 16) | chunkK);
		
		if (chunkHeight == null)
		{
			chunkHeight = 0;
			Chunk chunk = worldObj.getChunkFromChunkCoords(chunkI, chunkK);
			for (int i = 0; i < chunk.heightMap.length; i++)
			{
				if (chunk.heightMap[i] > chunkHeight) chunkHeight = (int)chunk.heightMap[i];
			}
			maxChunkHeightCache.put((chunkI << 16) | chunkK, chunkHeight);
			return chunkHeight;
		}
		else
		{
			return chunkHeight.intValue();
		}
	}

	public boolean moveEntityOptimized()
	{
		double deltaX = motionX;
		double deltaY = motionY;
		double deltaZ = motionZ;
		this.worldObj.theProfiler.startSection("zeppelin-moveEntityOptimized");
		float deltaYaw = (float) motionYaw;
		ArrayList<AxisAlignedBB> boundingBoxes = new ArrayList<AxisAlignedBB>();
		ArrayList<ArrayList<AxisAlignedBB>> collidingBoxes = new ArrayList<ArrayList<AxisAlignedBB>>();

		calculateMotionFromSpeedAndRotation();
		
		ySize *= 0.4F;
		double origDeltaX = deltaX;
		double origDeltaY = deltaY;
		double origDeltaZ = deltaZ;
		

		AxisAlignedBB totalBox = getRotatedTotalBoundingBox();
		//First, check to see if we hit the ceiling or floor of the world
		//TODO Test if this works with height mods. Theoretically it should work with both normal and cubic chunks.
		if (!worldObj.blockExists((int)posX, (int)(totalBox.maxY + deltaY), (int)posZ) && deltaY > 0)
		{
			deltaY = 0;
		}
		if (!worldObj.blockExists((int)posX, (int)(totalBox.minY + deltaY), (int)posZ) && deltaY < 0)
		{
			deltaY = 0;
		}

		int maxHeightValue = 256;
		totalBox = totalBox.expand(3, 3, 3);
		for (int i = MathHelper.floor_double(totalBox.minX); i < MathHelper.floor_double(totalBox.maxX + 1.0D); i++)
		{
			for (int j = MathHelper.floor_double(totalBox.minZ); j < MathHelper.floor_double(totalBox.maxZ + 1.0D); j++)
			{
				if (worldObj.getHeightValue(i, j) > maxHeightValue)
				{
					maxHeightValue = worldObj.getHeightValue(i, j);
				}
			}
		}

		if (maxHeightValue > totalBox.minY)
		{
			//Giant collision code
			
			//Two arrays are used.  One to hold the bounding box, and a sibling array to hold the boxes it collided with.
			//Keys match, but are not meaningful (except 0 which is this master entity)
			
		 
			//Get the colliding bounding boxes, for the entire craft, excluding players
			//Reset the arrays
			boundingBoxes = new ArrayList<AxisAlignedBB>();
			collidingBoxes = new ArrayList<ArrayList<AxisAlignedBB>>();
	
			if (!getGrid().collidableGridBlocks.isEmpty())
			{
				for (EntityBlock child : getGrid().collidableGridBlocks)
				{
					if (child.getBoundingBox() == null) continue;
					
					//Cull the block if it is above the height map for the chunk
					if (child.posY - 2 > getMaximumChunkHeight(child.chunkCoordX, child.chunkCoordZ)) 
					{
						continue;
					}
					
					ArrayList<AxisAlignedBB> childBoxes = this.getCollidingBoundingBoxesExcludingPlayers(worldObj, child, child.getBoundingBox().addCoord(deltaX, deltaY, deltaZ));
					if (childBoxes.size() > 0) // dont add it if theres nothing in it
					{
						deltaYaw = 0;
						boundingBoxes.add(child.getBoundingBox());
						collidingBoxes.add(childBoxes);
					}
				}
			}
			
			for(int childIndex = 0; childIndex < boundingBoxes.size(); childIndex++)
			{
				AxisAlignedBB box = boundingBoxes.get(childIndex);
				ArrayList<AxisAlignedBB> collidingBox = collidingBoxes.get(childIndex);
				
				for (int collidingBoxIndex = 0; collidingBoxIndex < collidingBox.size(); collidingBoxIndex++)
				{
					if (deltaX != 0) deltaX = (collidingBox.get(collidingBoxIndex)).calculateXOffset(box, deltaX);
					if (deltaY != 0) deltaY = (collidingBox.get(collidingBoxIndex)).calculateYOffset(box, deltaY);
					if (deltaZ != 0) deltaZ = (collidingBox.get(collidingBoxIndex)).calculateZOffset(box, deltaZ);
				}
				if (deltaX == 0 && deltaY == 0 && deltaZ == 0)
				{
					break;
				}
			}
		}
		
		rotationYaw += deltaYaw;
		rotationYaw = (rotationYaw + 360) % 360;
		prevRotationYaw = (float) (rotationYaw - deltaYaw);

		setPosition(posX + deltaX, posY + deltaY, posZ + deltaZ);
		isCollidedHorizontally = origDeltaX != deltaX || origDeltaZ != deltaZ;
		isCollidedVertically = origDeltaY != deltaY;
		onGround = origDeltaY != deltaY && origDeltaY < 0.0D;
		isCollided = isCollidedHorizontally || isCollidedVertically;
		//updateFallState(deltaY, onGround);
		if(origDeltaX != deltaX)
		{
			forwardSpeed = 0.0D;
			motionX = 0.0D;
		}
		if(origDeltaY != deltaY)
		{
			ascent = 0;
			motionY = 0.0D;
		}
		if(origDeltaZ != deltaZ)
		{
			forwardSpeed = 0.0D;
			motionZ = 0.0D;
		}
		

		
		
		if (deltaX != 0 || deltaY != 0 || deltaZ != 0 || deltaYaw != 0)
		{
			
			//Update the child block positions so entities can move and not collide
			getGrid().updateChildPositions();
			
			//Just move every entity on the ship that isn't another craft entity
			ArrayList<Entity> collidingEntities = getEntitiesOnCraft();
			for (Entity thing : collidingEntities)
			{
				if (thing instanceof EntityBlock)
				{
					//Do nothing
				}
				else
				{
					double centerX = this.posX - deltaX;
					double centerZ = this.posZ - deltaZ;
					
					double thingDeltaX = (thing.posX - centerX);
					double thingDeltaZ = (thing.posZ - centerZ);
					
					double thingRotatedPosX = ((thingDeltaX*Math.cos((float) (-deltaYaw/180*(Math.PI))) - thingDeltaZ*Math.sin((float) (-deltaYaw/180*(Math.PI)))) + centerX);
					double thingRotatedPosZ = ((thingDeltaX*Math.sin((float) (-deltaYaw/180*(Math.PI))) + thingDeltaZ*Math.cos((float) (-deltaYaw/180*(Math.PI)))) + centerZ);
					
					thing.rotationYaw -= deltaYaw;
					if (deltaY > 0){
						thing.posY += deltaY;
						thing.boundingBox.offset(0, deltaY, 0);
					}
					thing.boundingBox.offset(deltaX, 0, deltaZ);
					
					//thing.moveEntity(thingRotatedPosX - thing.posX + deltaX , thing.motionY, thingRotatedPosZ - thing.posZ + deltaZ);
					thing.moveEntity(thingRotatedPosX - thing.posX , thing.motionY, thingRotatedPosZ - thing.posZ);

				}
			}
			this.worldObj.theProfiler.endSection();
			return true;
		}
		else
		{
			this.worldObj.theProfiler.endSection();
			return false;
		}
		
	}
	
	private void calculateMotionFromSpeedAndRotation() {
		motionY = ascent; //No rotation effect on Y axis for now
		
		motionX = forwardSpeed*MathHelper.sin((float) ((rotationYaw+(initialRotation))/180*(Math.PI)));
		motionZ = forwardSpeed*MathHelper.cos((float) ((rotationYaw+(initialRotation))/180*(Math.PI)));
	}
//
//	@Override
//	public void moveEntity(double deltaX, double deltaY, double deltaZ)
//	{
//		ArrayList<AxisAlignedBB> boundingBoxes = new ArrayList<AxisAlignedBB>();
//		ArrayList<ArrayList<AxisAlignedBB>> collidingBoxes = new ArrayList<ArrayList<AxisAlignedBB>>();
//
//		ySize *= 0.4F;
//		double origDeltaX = deltaX;
//		double origDeltaY = deltaY;
//		double origDeltaZ = deltaZ;
//		
//
//		AxisAlignedBB totalBox = getRotatedTotalBoundingBox();
//		//First, check to see if we hit the ceiling or floor of the world
//		//TODO Test if this works with height mods. Theoretically it should work with both normal and cubic chunks.
//		if (!worldObj.blockExists((int)posX, (int)(totalBox.maxY + deltaY), (int)posZ) ||
//			!worldObj.blockExists((int)posX, (int)(totalBox.minY + deltaY), (int)posZ))
//		{
//			deltaY = 0;
//		}
//
//		int maxHeightValue = 0;
//		totalBox = totalBox.expand(3, 3, 3);
//		for (int i = MathHelper.floor_double(totalBox.minX); i < MathHelper.floor_double(totalBox.maxX + 1.0D); i++)
//		{
//			for (int j = MathHelper.floor_double(totalBox.minZ); j < MathHelper.floor_double(totalBox.maxZ + 1.0D); j++)
//			{
//				if (worldObj.getHeightValue(i, j) > maxHeightValue)
//				{
//					maxHeightValue = worldObj.getHeightValue(i, j);
//				}
//			}
//		}
//
//		if (maxHeightValue > totalBox.minY)
//		{
//			//Giant collision code
//			
//			//Two arrays are used.  One to hold the bounding box, and a sibling array to hold the boxes it collided with.
//			//Keys match, but are not meaningful (except 0 which is this master entity)
//			
//		 
//			//Get the colliding bounding boxes, for the entire craft, excluding players
//			//Reset the arrays
//			boundingBoxes = new ArrayList<AxisAlignedBB>();
//			collidingBoxes = new ArrayList<ArrayList<AxisAlignedBB>>();
//	
//			//boundingBoxes.add(this.getBoundingBox());
//			//collidingBoxes.add(this.getCollidingBoundingBoxesExcludingPlayers(worldObj, this, getBoundingBox().addCoord(deltaX, deltaY, deltaZ)));
//			
//			if (!getGrid().collidableGridBlocks.isEmpty())
//			{
//				for (EntityBlock child : getGrid().collidableGridBlocks)
//				{
//					if (child.getBoundingBox() == null) continue;
//					
//					ArrayList<AxisAlignedBB> childBoxes = this.getCollidingBoundingBoxesExcludingPlayers(worldObj, child, child.getBoundingBox().addCoord(deltaX, deltaY, deltaZ));
//					if (childBoxes.size() > 0) // dont add it if theres nothing in it
//					{
//						boundingBoxes.add(child.getBoundingBox());
//						collidingBoxes.add(childBoxes);
//					}
//				}
//			}
//			
//	
//			for(int childIndex = 0; childIndex < boundingBoxes.size(); childIndex++)
//			{
//				AxisAlignedBB box = boundingBoxes.get(childIndex);
//				ArrayList<AxisAlignedBB> collidingBox = collidingBoxes.get(childIndex);
//				
//				for (int collidingBoxIndex = 0; collidingBoxIndex < collidingBox.size(); collidingBoxIndex++)
//				{
//					if (deltaX != 0) deltaX = (collidingBox.get(collidingBoxIndex)).calculateXOffset(box, deltaX);
//					if (deltaY != 0) deltaY = (collidingBox.get(collidingBoxIndex)).calculateYOffset(box, deltaY);
//					if (deltaZ != 0) deltaZ = (collidingBox.get(collidingBoxIndex)).calculateZOffset(box, deltaZ);
//				}
//				if (deltaX == 0 && deltaY == 0 && deltaZ == 0)
//				{
//					break;
//				}
//			}
//		}
//
//		double deltaTotal = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
//		ModLoader.getMinecraftInstance().thePlayer.addStat(mod_Zeppelin.statDistanceByCraft, (int)(deltaTotal * 100));
//		setPosition(posX + deltaX, posY + deltaY, posZ + deltaZ);
//		isCollidedHorizontally = origDeltaX != deltaX || origDeltaZ != deltaZ;
//		isCollidedVertically = origDeltaY != deltaY;
//		onGround = origDeltaY != deltaY && origDeltaY < 0.0D;
//		isCollided = isCollidedHorizontally || isCollidedVertically;
//		//updateFallState(deltaY, onGround);
//		if(origDeltaX != deltaX)
//		{
//			forwardSpeed = 0.0D;
//			motionX = 0.0D;
//		}
//		if(origDeltaY != deltaY)
//		{
//			ascent = 0;
//			motionY = 0.0D;
//		}
//		if(origDeltaZ != deltaZ)
//		{
//			forwardSpeed = 0.0D;
//			motionZ = 0.0D;
//		}
//		
//
//		
//		
//		if (deltaX != 0 || deltaY != 0 || deltaZ != 0)
//		{
//			//Just move every entity on the ship that isn't another craft entity
//			
//			
//			ArrayList<Entity> collidingEntities = getEntitiesOnCraft();
//					
//			for (Entity thing : collidingEntities)
//			{
//				if (thing instanceof EntityBlock)
//				{
//					//Do nothing
//				}
//				else
//				{
//					thing.posY += deltaY;
//					thing.boundingBox.offset(0, deltaY, 0);
//					thing.moveEntity(deltaX + thing.motionX, thing.motionY, deltaZ + thing.motionZ);
//				}
//			}
//		}
//	}

	private AxisAlignedBB getRotatedTotalBoundingBox() {
		return RotatedBB.getBoundingBoxFromPool(getGrid().getTotalBoundingBox().offset(posX, posY, posZ), rotationYaw).getExpandedAABB();
	}

	private ArrayList<Entity> cachedEntitiesOnCraft;
	private int getEntitiesOnCraftCounter = 100;
	public ArrayList<Entity> getEntitiesOnCraft()
	{
		this.worldObj.theProfiler.startSection("getEntitiesOnCraft");
		getEntitiesOnCraftCounter++;
		if (getEntitiesOnCraftCounter < MasterCraftEntity.entityCacheTime){
			//Return the cached entities, but make sure they still exist (arrows/TNT/etc)
			for (Iterator<Entity> i = cachedEntitiesOnCraft.iterator(); i.hasNext();)
			{
				Entity entity = i.next();
				if (entity == null || entity.isDead || entity instanceof EntityBlock)
				{
					i.remove();
				}
			}
			return cachedEntitiesOnCraft;
		}
		getEntitiesOnCraftCounter = 0;
		
		AxisAlignedBB expandedCaptureBox = getRotatedTotalBoundingBox().expand(3, 3, 3);
		@SuppressWarnings("unchecked")
		ArrayList<Entity> entities = (ArrayList<Entity>)worldObj.getEntitiesWithinAABB(Entity.class, expandedCaptureBox);
		
		for (Iterator<Entity> i = entities.iterator(); i.hasNext();)
		{
			Entity entity = i.next();
			if (entity instanceof EntityBlock || !isEntityOnCraft(entity))
			{
				i.remove();
			}
		}
		cachedEntitiesOnCraft = entities;
		this.worldObj.theProfiler.endSection();
		return cachedEntitiesOnCraft;
	}

	public boolean isEntityOnCraft(Entity entity)
	{
		boolean retVal = false;
		WorldGrid grid = getGrid();
		
		if (Ship.accurateIsPlayerOnCraft)
		{
			double d = 2.25D;
			AxisAlignedBB boxToTest = entity.boundingBox.expand(d, 0, d);
			
			boxToTest.minY -= 3;
			
			@SuppressWarnings("rawtypes")
			List collidingEntities = worldObj.getEntitiesWithinAABBExcludingEntity(entity, boxToTest);			
			
			for(int entityIndex = 0; entityIndex < collidingEntities.size(); entityIndex++)
			{
				Entity tempEntity = (Entity)collidingEntities.get(entityIndex);
				
				//Find if the entitys box collided with any craftentities of ours
				if (tempEntity instanceof EntityBlock)
				{
					EntityBlock tempEntityBlock = (EntityBlock)tempEntity;
					if (tempEntityBlock.getGrid()!= null && tempEntityBlock.getGrid().equals(this.getGrid()))
					{
						//It is a child, we have collision
						retVal = true;
						break;
					}
				}
			}
			
		} 
		else 
		{
			AxisAlignedBB box = entity.boundingBox.expand(0.5, 2, 0.5);
			if (grid.getTotalBoundingBox().intersectsWith(box))
			{
				retVal = true;
			}
		}
		return retVal;
	}

	private ArrayList<AxisAlignedBB> getCollidingBoundingBoxesExcludingPlayers(World world, Entity entity, AxisAlignedBB axisalignedbb)
	{
		this.worldObj.theProfiler.startSection("getCollidingBoundingBoxesExcludingEntity");
//		//Array used to hold all of the bounding boxes we find
		ArrayList<AxisAlignedBB> collidingBoundingBoxes = new ArrayList<AxisAlignedBB>();
		collidingBoundingBoxes.clear();
		
		int blockMinX = MathHelper.floor_double(axisalignedbb.minX);
		int blockMaxX = MathHelper.floor_double(axisalignedbb.maxX + 1.0D);
		int blockMinY = MathHelper.floor_double(axisalignedbb.minY);
		int blockMaxY = MathHelper.floor_double(axisalignedbb.maxY + 1.0D);
		int blockMinZ = MathHelper.floor_double(axisalignedbb.minZ);
		int blockMaxZ = MathHelper.floor_double(axisalignedbb.maxZ + 1.0D);
		for(int i = blockMinX; i < blockMaxX; i++)
		{
			for(int k = blockMinZ; k < blockMaxZ; k++)
			{
				//64 is used for j axis since it is only sanity checked and not used
				if((!world.blockExists(i, 64, k) || world.getHeightValue(i, k) < blockMinY) && !(world instanceof WorldGrid))
				{
					continue;
				}
				
				//Check all neighboring blocks for collision
				for(int j = blockMinY - 1; j < blockMaxY; j++)
				{
					Block block = Block.blocksList[world.getBlockId(i, j, k)];
					if(block != null)
					{
						block.getCollisionBoundingBoxFromPool(world, i, j, k);
					}
				}
			}
		}

		this.worldObj.theProfiler.endSection();
		return collidingBoundingBoxes;
	}
	

	/*
	 * LOAD / SAVE
	 */

	/*
	 * This function is required for the minecraft engine to save the grid.  Since this 
	 * Master Craft Entity is the only 'real' entity the game has registered, it is the only
	 * one that will be persisted.  Therefore it is responsible for saving and reloading
	 * the grid.
	 */
	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{		
		
		//Master Craft Entity specific stuff
		
		blockId = nbttagcompound.getInteger("blockId");
		meta = nbttagcompound.getInteger("meta");
		
		setCraftID(nbttagcompound.getInteger("craftID"));
		
		initialRotation = nbttagcompound.getDouble("initialRotation");
		
		//Reload children
		setGrid(new WorldGrid(this));
		getGrid().loadGridFromNBT(nbttagcompound.getTagList("grid"));
		
		//Enter all the gridblocks to the world
		if (!getGrid().gridBlocks.isEmpty())
		{
			getGrid().updateChildPositions();
			for (EntityBlock child : getGrid().gridBlocks)
			{
				worldObj.spawnEntityInWorld(child);
			}
			//Put this entity at the end of the list (after the children is the important part)
			while (worldObj.loadedEntityList.remove(this));
			worldObj.spawnEntityInWorld(this);
		}
		
		calculateMotionFromSpeedAndRotation();
		
		CraftManager.recoverCraft(this);
	}
	
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{		
		//Master Craft Entity specific stuff
		
		nbttagcompound.setInteger("blockId", blockId);
		nbttagcompound.setInteger("meta", meta);
		
		nbttagcompound.setInteger("craftID", getCraftID());
		nbttagcompound.setDouble("initialRotation", initialRotation);
		
		//Save the grid		
		nbttagcompound.setTag("grid", getGrid().saveGridToNBT());
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * CONTROLS
	 */
	public void setForwardSpeed(double speed)
	{
		forwardSpeed = speed;
		calculateMotionFromSpeedAndRotation();
	}
	
	public double getForwardSpeed() 
	{
		return forwardSpeed;
	}
	
	public void setMotionYaw(double d)
	{
		motionYaw = d;
		if (-0.0001d < motionYaw && motionYaw < 0.0001d) motionYaw = 0;
		calculateMotionFromSpeedAndRotation();
	}
	
	public double getMotionYaw()
	{
		return motionYaw;
	}
	
	public void setAscent(double d)
	{
		ascent = d;
		if (-0.0001d < ascent && ascent < 0.0001d) ascent = 0;
		calculateMotionFromSpeedAndRotation();
	}

	public double getAscent() {
		return ascent;
	}
	
	/*
	 * Interaction on MasterCraftEntities will cause the ship to stop and align.  The CraftController
	 * will then be destroyed, and the child blocks will be recreated as normal blocks.
	 * @see net.minecraft.src.MoveCraftAdvanced.CraftEntity#interact(net.minecraft.src.EntityPlayer)
	 */
	@Override
	public boolean interactFirst(EntityPlayer entityplayer)
	{
		if (!this.isDead)
		{
			destroy(false);
			return true;
		}
		else
		{
			List<Entity> tmpList = new ArrayList<Entity>();
			
			//TERRIBLE HACK JOB
			for (int i = 0; i < 400; i++) tmpList.add(this);
			
			worldObj.unloadEntities(tmpList);
			return false;
		}
	}



	public void stop() 
	{
		forwardSpeed = 0;
		motionYaw = 0;
		ascent = 0;		
	}
	
	/*
	 * Places the ship on a grid aligned coordinate system. 
	 */
	public boolean align()
	{
		float newRotationYaw = Math.round(rotationYaw / 90) * 90 % 360;
		double deltaYaw = -(rotationYaw - newRotationYaw);
		double deltaX = Math.round(posX + 0.5) - posX - 0.5;
		double deltaY = Math.round(posY + 0.5) - posY - 0.5;
		double deltaZ = Math.round(posZ + 0.5) - posZ - 0.5;
		
		if (!getGrid().collidableGridBlocks.isEmpty())
		{
			for (EntityBlock child : getGrid().collidableGridBlocks)
			{
				if (child.getBoundingBox() == null) continue;
				
				AxisAlignedBB boxToTest = RotatedBB.getBoundingBoxFromPool(child.getBoundingBox()).rotateAroundVec3(Vec3.createVectorHelper(posX, posY, posZ), deltaYaw).getExpandedAABB();
				ArrayList<AxisAlignedBB> childBoxes = getCollidingBoundingBoxesExcludingPlayers(worldObj, child, boxToTest);
				if (childBoxes.size() > 0) 
				{
					//Cant fit!
					return false;
				}
			}
		}
		
		//We can fit, do the alignment
		prevRotationYaw = rotationYaw;
		rotationYaw = newRotationYaw;
		posX += deltaX;
		posY += deltaY;
		posZ += deltaZ;
		calculateMotionFromSpeedAndRotation();
		getGrid().updateChildPositions();
		
		//Now move all the entities onboard
		ArrayList<Entity> collidingEntities = getEntitiesOnCraft();
		for (Entity thing : collidingEntities)
		{
			if (thing instanceof EntityBlock)
			{
				//Do nothing
			}
			else
			{
				{					
					double centerX = this.posX - deltaX;
					double centerZ = this.posZ - deltaZ;
					
					double thingDeltaX = (thing.posX - centerX);
					double thingDeltaZ = (thing.posZ - centerZ);
					
					double thingRotatedPosX = ((thingDeltaX*Math.cos((float) (-deltaYaw/180*(Math.PI))) - thingDeltaZ*MathHelper.sin((float) (-deltaYaw/180*(Math.PI)))) + centerX);
					double thingRotatedPosZ = ((thingDeltaX*Math.sin((float) (-deltaYaw/180*(Math.PI))) + thingDeltaZ*MathHelper.cos((float) (-deltaYaw/180*(Math.PI)))) + centerZ);
					
					thing.rotationYaw -= deltaYaw;
					thing.posY += deltaY;
					thing.boundingBox.offset(0, deltaY, 0);
					thing.moveEntity(thingRotatedPosX - thing.posX + deltaX , thing.motionY, thingRotatedPosZ - thing.posZ + deltaZ);

				}
			}
		}
		
		return true;
	}
}
