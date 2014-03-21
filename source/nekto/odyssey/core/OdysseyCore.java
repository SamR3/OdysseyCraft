package nekto.odyssey.core;

import nekto.odyssey.entity.EntityBlock;
import nekto.odyssey.entity.EntityBlockConsole;
import nekto.odyssey.lib.GeneralRef;
import nekto.odyssey.network.CommonProxy;
import nekto.odyssey.network.OdysseyPacketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = GeneralRef.MODID, name = "Odyssey Craft", version = GeneralRef.VERSION)
@NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = {
		GeneralRef.BLOCK_UPDATE_CHANNEL, GeneralRef.KEY_CHANGE_CHANNEL }, packetHandler = OdysseyPacketHandler.class)
public class OdysseyCore
{
	@Instance(value = GeneralRef.MODID)
	public static OdysseyCore instance;

	@SidedProxy(clientSide = "nekto.odyssey.client.ClientProxy", serverSide = "nekto.odyssey.network.CommonProxy")
	public static CommonProxy proxy;

	public static Block console;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.registerRenderers();

		KeyBinding[] key = { new KeyBinding("I", Keyboard.KEY_I),
				new KeyBinding("K", Keyboard.KEY_K),
				new KeyBinding("J", Keyboard.KEY_J),
				new KeyBinding("L", Keyboard.KEY_L) };
		boolean[] repeat = { false, false, false, false };

		console = new BlockConsole(500, Material.rock)
				.setUnlocalizedName("console");
		GameRegistry.registerBlock(console, GeneralRef.MODID
				+ console.getUnlocalizedName().substring(5));
	}

	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		EntityRegistry.registerModEntity(EntityBlock.class, "EntityBlock",
				EntityRegistry.findGlobalUniqueEntityId(), instance, 80, 3,
				false);
		EntityRegistry.registerModEntity(EntityBlockConsole.class,
				"EntityBlockConsole",
				EntityRegistry.findGlobalUniqueEntityId(), instance, 80, 3,
				false);
	}

}
