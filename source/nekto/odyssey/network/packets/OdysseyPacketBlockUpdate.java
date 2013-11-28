package nekto.odyssey.network.packets;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

public class OdysseyPacketBlockUpdate extends OdysseyPacket
{

	@Override
	public void handle(DataInputStream iStream, EntityPlayer player)
	{
		
		int x;
		int y;
		int z;
		
		try {
			x = iStream.readInt();
			y = iStream.readInt();
			z = iStream.readInt();
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
		
		player.worldObj.setBlockToAir(x, y, z);
	}

}
