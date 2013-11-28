package nekto.odyssey.core;

import nekto.odyssey.craft.CraftManager;
import nekto.odyssey.tile.TileEntityConsole;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockConsole extends BlockContainer
{

	public BlockConsole(int par1, Material par2Material)
	{
		super(par1, par2Material);
		setCreativeTab(CreativeTabs.tabBlock);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityConsole();
	}

	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
		if (par1World.isRemote)
        {
        	CraftManager.spawnCraft(par1World, par2, par3, par4, this.blockID, par1World.getBlockMetadata(par2, par3, par4), par5EntityPlayer);
        }
		
    	par1World.setBlockToAir(par2, par3, par4);
        	
        return true;
    }
}
