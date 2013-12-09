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
	
	private void activateController(World world, int i, int j, int k)
	{
		CraftManager.getInstance().createShipFromConsole(world, i, j, k);
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9)	
	{
		activateController(world, i, j, k);
		return true;
	}
}
