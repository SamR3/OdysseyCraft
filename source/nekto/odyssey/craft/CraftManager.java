package nekto.odyssey.craft;

import java.util.ArrayList;

public class CraftManager
{
	private static CraftManager instance = null;
	private static ArrayList<Ship> registeredShips = new ArrayList<Ship>();
	
	public static CraftManager getInstance()
	{
		if(instance == null)
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
		
	}
	
	public String createShipFromConsole(World world, int x, int y, int z)
	{
		
	}
	
	/*public static Map<EntityBlockConsole, EntityPlayer> ships = new HashMap<EntityBlockConsole, EntityPlayer>();
	private static World server;
	
	public static void spawnCraft(World world, int x, int y, int z, int id, int meta, EntityPlayer player)
	{
		EntityBlockConsole ent = new EntityBlockConsole(world, x, y, z, id, meta);
		ships.put(ent, player);
		
		server = world;
		
		server.spawnEntityInWorld(ent);
	}
	
	public static void setSpeeds(float x, float y, float z, float yaw)
	{
		for(EntityBlockConsole ent : ships.keySet())
		{
			ent.setSpeed(x, y, z, yaw);
			System.out.println("Set entity speed: " + x + ", " + y + ", " + z);
		}
	}
	
	public static void spawnChildEntity(World par1World, int x, int y, int z, int id, int meta, EntityBlockConsole console)
	{
		EntityBlock ent = new EntityBlock(par1World, x, y, z, id, meta, console);
		console.addChild(ent);
		
		server.spawnEntityInWorld(ent);
	}*/
}
