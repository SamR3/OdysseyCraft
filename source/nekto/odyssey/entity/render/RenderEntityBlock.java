package nekto.odyssey.entity.render;

import nekto.odyssey.entity.EntityBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class RenderEntityBlock extends Render
{
	Block render;
	int meta;
	
	public RenderEntityBlock()
    {
        this.shadowSize = 0.5F;
    }

	@Override
	public void doRender(Entity entity, double x, double y, double z, float rotationYaw, float entityBrightness)
	{
		EntityBlock blockEntity = (EntityBlock) entity;
		
		int blockId = blockEntity.blockId;
		meta = blockEntity.meta;
		
		RenderBlocks blockRenderer = new RenderBlocks();
		
		GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        
        this.bindEntityTexture(blockEntity);
        blockRenderer.renderBlockAsItem(Block.blocksList[blockId], 0, blockEntity.getBrightness(entityBrightness));
     
        GL11.glPopMatrix();
    }

    protected ResourceLocation getEntityTexture(Entity par1Entity)
    {
        return TextureMap.locationBlocksTexture;
    }
}
