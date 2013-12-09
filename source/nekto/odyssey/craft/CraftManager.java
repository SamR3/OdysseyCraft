package nekto.odyssey.craft;

import java.util.ArrayList;
import java.util.HashSet;

import nekto.math.traversal.BlockPositionWrapper;
import nekto.math.traversal.Traverser;
import net.minecraft.world.World;

public class CraftManager
{
	private static CraftManager instance = null;
	private static ArrayList<Ship> registeredShips = new ArrayList<Ship>();

	public static CraftManager getInstance()
	{
		if (instance == null)
		{
			instance = new CraftManager();
		}

		return instance;
	}

	protected CraftManager()
	{

	}

	public static void registerShip(Ship ship)
	{
		registeredShips.add(ship);
	}

	public String createShipFromConsole(World world, int x, int y, int z)
	{
		Traverser traverser = new Traverser(x, y, z, world);
		BlockPositionWrapper parent = new BlockPositionWrapper(x, y, z);
		
		HashSet<BlockPositionWrapper> foundBlocks = traverser.beginTraversal();
		registerShip(new Ship(world, parent, foundBlocks));
		
		return "Found ship of size " + foundBlocks.size() + ".";	
	}
	
	public void craftDestroyed()
	{
		if(!registeredShips.isEmpty())
		{
			for(Ship ship : registeredShips)
			{
				if(ship.isCraftDestroyed())
				{
					registeredShips.remove(ship);
					break;
				}
			}
		}
	}
	
	public void increaseSpeed()
	{
		for (Ship ship : registeredShips)
		{
			if (ship.isPlayerOnCraft())
			{
				ship.increaseSpeed();
			}
		}
	}
	
	public void decreaseSpeed()
	{
		for (Ship ship : registeredShips)
		{
			if (ship.isPlayerOnCraft())
			{
				ship.decreaseSpeed();
			}
		}
	}

	public void rotateLeft() {
		for (Ship ship : registeredShips)
		{
			if (ship.isPlayerOnCraft())
			{
				ship.rotateLeft();
			}
		}
	}

	public void rotateRight() {
		for (Ship ship : registeredShips)
		{
			if (ship.isPlayerOnCraft())
			{
				ship.rotateRight();
			}
		}
	}

	public void ascend() {
		for (Ship ship : registeredShips)
		{
			if (ship.isPlayerOnCraft())
			{
				ship.ascend();
			}
		}
	}

	public void descend() {
		for (Ship ship : registeredShips)
		{
			if (ship.isPlayerOnCraft())
			{
				ship.descend();
			}
		}
	}

	public void stop() {
		for (Ship ship : registeredShips)
		{
			if (ship.isPlayerOnCraft())
			{
				ship.stop();
			}
		}
	}
	
	public void align() {
		for (Ship ship : registeredShips)
		{
			if (ship.isPlayerOnCraft())
			{
				ship.align();
			}
		}
	}

//	private static Ship getCraftByID(int craftID) 
//	{
//		if (!registeredShips.isEmpty())
//		{
//			for (Ship ship : registeredShips)
//			{
//				if (ship.getCraftID() == craftID)
//				{
//					return craft;
//				}
//			}
//		}
//		return null;
//	}

	/*
	 * Searches the registered ship list for an existing ship with this ID.
	 * If found, calls destroy, and unregisters it.
	 */
	private static void destroyExistingCraft(int craftID) {
		// TODO Auto-generated method stub
		if (!registeredShips.isEmpty())
		{
			for (Ship ship : registeredShips)
			{
				if (ship.getCraftID() == craftID)
				{
					ship.destroy();
					registeredShips.remove(ship);
					return;
				}
			}
		}
	}

	/*
	 * Thread safe method to get the next available ship ID.
	 */
	public synchronized int getNextCraftID() 
	{
		if (registeredShips.isEmpty())
		{
			return 1;
		} 
		else
		{
			int maxID = 0;
			for (Ship ship : registeredShips)
			{
				if (ship.getCraftID() > maxID)
				{
					maxID = ship.getCraftID();
				}
			}
			return maxID + 1;
		}
	}

	
	/*
	 * This method will create a new ship based around the existing master
	 * ship entity passed in.  If a ship with the same ID already exists
	 * (usually in the case of exiting to main screen and reloading) then
	 * that ship will be destroyed in favour of this one.
	 */
	public static void recoverCraft(MasterCraftEntity recoveredCraft)
	{
		destroyExistingCraft(recoveredCraft.getCraftID());
		registerShip(new Ship(recoveredCraft));
	}
	
	public static void tickCraft()
	{
		for (Ship ship : registeredShips)
		{
			ship.masterCraftEntity.getGrid().tick();
		}
	}

	/*
	 * public static Map<EntityBlockConsole, EntityPlayer> ships = new
	 * HashMap<EntityBlockConsole, EntityPlayer>(); private static World server;
	 * 
	 * public static void spawnCraft(World world, int x, int y, int z, int id,
	 * int meta, EntityPlayer player) { EntityBlockConsole ent = new
	 * EntityBlockConsole(world, x, y, z, id, meta); ships.put(ent, player);
	 * 
	 * server = world;
	 * 
	 * server.spawnEntityInWorld(ent); }
	 * 
	 * public static void setSpeeds(float x, float y, float z, float yaw) {
	 * for(EntityBlockConsole ent : ships.keySet()) { ent.setSpeed(x, y, z,
	 * yaw); System.out.println("Set entity speed: " + x + ", " + y + ", " + z);
	 * } }
	 * 
	 * public static void spawnChildEntity(World par1World, int x, int y, int z,
	 * int id, int meta, EntityBlockConsole console) { EntityBlock ent = new
	 * EntityBlock(par1World, x, y, z, id, meta, console);
	 * console.addChild(ent);
	 * 
	 * server.spawnEntityInWorld(ent); }
	 */
}
