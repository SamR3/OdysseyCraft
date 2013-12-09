package nekto.odyssey.craft;

import java.util.HashSet;

import nekto.math.traversal.BlockPositionWrapper;
import nekto.odyssey.entity.EntityBlock;
import nekto.odyssey.world.WorldGrid;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class Ship
{
	public static int MAX_SHIP_SIZE = 1;
	public static boolean accurateIsPlayerOnCraft = true;
	
	private int craftID = 0;
	private WorldGrid grid;
	
	private boolean canCraftRotate = true;
	private boolean canCraftY = true;
	private boolean canCraftXZ = true;
	
	private float speedIncrement = 0.05f;
	private float rotationIncrement = 0.1f;
	private float ascentIncrement = 0.01f;
	
	private float speedMaxPos = 20 *speedIncrement;
	private float speedMaxNeg = -1 *speedIncrement;
	
	private float rotationMaxPos =  50 * rotationIncrement;
	private float rotationMaxNeg = -50 * rotationIncrement;
	
	private float ascentMaxPos =  100 * ascentIncrement;
	private float ascentMaxNeg = -100 * ascentIncrement;
	
	public MasterCraftEntity masterCraftEntity;
	
	public Ship(MasterCraftEntity console)
	{
		this.masterCraftEntity = console;
		this.grid = console.getGrid();
		this.craftID = console.getCraftID();
		setMovementAbilityFromMetadata(console.meta);
	}
	
	public Ship(World world, BlockPositionWrapper parent, HashSet<BlockPositionWrapper> blocks)
	{
		this.craftID = CraftManager.getInstance().getNextCraftID();
		masterCraftEntity = new MasterCraftEntity(world);
		this.grid = new WorldGrid(masterCraftEntity);
		
		masterCraftEntity.setCraftID(this.craftID);
		masterCraftEntity.setPosition(parent.x + 0.5, parent.y + 0.5, parent.z + 0.5);
		masterCraftEntity.blockId = world.getBlockId(parent.x, parent.y, parent.z);
		masterCraftEntity.meta = world.getBlockMetadata(parent.x, parent.y, parent.z);
		
		setMovementAbilityFromMetadata(masterCraftEntity.meta);
		
		masterCraftEntity.initialRotation = -(masterCraftEntity.meta & 0x3) * 90;
		
		
		for(BlockPositionWrapper block : blocks)
		{
			if(!block.equals(parent))
			{
				masterCraftEntity.addChildBlock(
						world.getBlockId(block.x, block.y, block.z),
						world.getBlockMetadata(block.x, block.y, block.z),
						block.x - parent.x,
						block.y - parent.y,
						block.z - parent.z);
			}
		}
		
		grid.notifyBlocksCreation();
		
		for(BlockPositionWrapper block : blocks)
		{
			world.setBlock(block.x, block.y, block.z, 0, 1, 2);
		}
		
		world.spawnEntityInWorld(masterCraftEntity);
	}
	
	private void setMovementAbilityFromMetadata(int meta)
	{
		canCraftRotate = true;
		canCraftY = false;
		canCraftXZ = true;
	}
	
	public boolean isCraftDestroyed()
	{
		return masterCraftEntity.isDead;
	}
	
	public void destroy()
	{
		if (masterCraftEntity != null)
		{
			if (!masterCraftEntity.getGrid().gridBlocks.isEmpty())
			{
				for (EntityBlock child : masterCraftEntity.getGrid().gridBlocks)
				{
					child.setDead();
				}
			}
			masterCraftEntity.setDead();
		}
	}

	public boolean isPlayerOnCraft() 
	{
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
	
		return masterCraftEntity.isEntityOnCraft(player);
	}

	public void rotateLeft()
	{
		if (canCraftRotate)
		{
			double newyaw = masterCraftEntity.getMotionYaw() + rotationIncrement;
			
			if (newyaw > rotationMaxPos) newyaw = rotationMaxPos;
			if (newyaw < rotationMaxNeg) newyaw = rotationMaxNeg;
			masterCraftEntity.setMotionYaw(newyaw);
			printData();
		}
	}

	public void rotateRight()
	{
		if (canCraftRotate)
		{
			double newyaw = masterCraftEntity.getMotionYaw() - rotationIncrement;
			
			if (newyaw > rotationMaxPos) newyaw = rotationMaxPos;
			if (newyaw < rotationMaxNeg) newyaw = rotationMaxNeg;
			masterCraftEntity.setMotionYaw(newyaw);
			printData();
		}
	}

	public void ascend()
	{
		if (canCraftY)
		{
			double newascent = masterCraftEntity.getAscent() + ascentIncrement;
			if (newascent > ascentMaxPos) newascent = ascentMaxPos;
			if (newascent < ascentMaxNeg) newascent = ascentMaxNeg;
			
			masterCraftEntity.setAscent(newascent);
			printData();
		}
	}

	public void descend()
	{
		if (canCraftY)
		{
			double newascent = masterCraftEntity.getAscent() - ascentIncrement;
			if (newascent > ascentMaxPos) newascent = ascentMaxPos;
			if (newascent < ascentMaxNeg) newascent = ascentMaxNeg;
			
			masterCraftEntity.setAscent(newascent);
			printData();
		}
	}
	
	public void increaseSpeed()
	{
		if (canCraftXZ)
		{
			double newSpeed = masterCraftEntity.getForwardSpeed() + speedIncrement;
			if (newSpeed > speedMaxPos) newSpeed = speedMaxPos;
			if (newSpeed < speedMaxNeg) newSpeed = speedMaxNeg;
			
			masterCraftEntity.setForwardSpeed(newSpeed);
			printData();
		}
	}
	
	public void decreaseSpeed()
	{
		if (canCraftXZ)
		{
			double newSpeed = masterCraftEntity.getForwardSpeed() - speedIncrement;
			if (newSpeed > speedMaxPos) newSpeed = speedMaxPos;
			if (newSpeed < speedMaxNeg) newSpeed = speedMaxNeg;
			
			masterCraftEntity.setForwardSpeed(newSpeed);
			printData();
		}
	}
	
	public void stop() 
	{
		masterCraftEntity.stop();
		printData();
	}

	public void align() 
	{
		boolean aligned = masterCraftEntity.align();
		printData();
		if (!aligned)
		{
		}
	}
	
	
	private void printData()
	{
		if (true)
		{
			int yawRate = (int)Math.round(masterCraftEntity.getMotionYaw()/rotationIncrement);
			int ascent = (int)Math.round(masterCraftEntity.getAscent()/ascentIncrement);
			int speed = (int)Math.round(masterCraftEntity.getForwardSpeed()/speedIncrement);
			
			StringBuilder output = new StringBuilder();
			
			if (canCraftXZ) output = output.append("Speed: ").append(speed).append(" ");
			if (canCraftRotate) output = output.append("Yawrate: ").append(yawRate).append(" ");
			if (canCraftY) output = output.append("Ascent: ").append(ascent);
			
			Minecraft.getMinecraft().ingameGUI.getChatGUI().clearChatMessages();
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(output.toString());
		}
	}

	public int getCraftID() 
	{
		return this.craftID;
	}
}
