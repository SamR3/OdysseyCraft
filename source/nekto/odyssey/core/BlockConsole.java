package nekto.odyssey.core;

import nekto.odyssey.craft.CraftManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class BlockConsole extends Block
{

	public BlockConsole(int par1, Material par2Material)
	{
		super(par1, par2Material);
		setCreativeTab(CreativeTabs.tabBlock);
	}

	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
		if (par1World.isRemote)
        {
			//TODO: use craft registry
        	//CraftManager.spawnCraft(par1World, par2, par3, par4, this.blockID, par1World.getBlockMetadata(par2, par3, par4), par5EntityPlayer);
        }
		
    	par1World.setBlockToAir(par2, par3, par4);
        	
        return true;
    }
}
