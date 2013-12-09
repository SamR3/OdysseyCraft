package nekto.odyssey.client;

import nekto.odyssey.entity.EntityBlock;
import nekto.odyssey.entity.EntityBlockConsole;
import nekto.odyssey.entity.render.RenderEntityBlock;
import nekto.odyssey.network.CommonProxy;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void registerRenderers()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityBlock.class,
				new RenderEntityBlock());
		RenderingRegistry.registerEntityRenderingHandler(
				EntityBlockConsole.class, new RenderEntityBlock());
	}
}
