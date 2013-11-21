package nekto.odyssey.core;

import nekto.odyssey.lib.GeneralRef;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = GeneralRef.MODID, name = "Odyssey Craft", version = GeneralRef.VERSION)
@NetworkMod(clientSideRequired = true, serverSideRequired = false)

public class OdysseyCore
{
	public static Block console;
	
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		console = new BlockConsole(500,
				Material.rock).setUnlocalizedName("console");
		GameRegistry.registerBlock(console, GeneralRef.MODID + console.getUnlocalizedName().substring(5));
		
	}

}
