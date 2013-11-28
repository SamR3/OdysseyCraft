package nekto.odyssey.craft;

import java.util.HashMap;
import java.util.Map;

import nekto.odyssey.entity.EntityBlock;
import nekto.odyssey.entity.EntityBlockConsole;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class CraftManager
{
	public static Map<EntityBlockConsole, EntityPlayer> ships = new HashMap<EntityBlockConsole, EntityPlayer>();
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
	
	public static void spawnChildEntity(EntityBlock ent, EntityBlockConsole parent)
	{
		
	}
}
