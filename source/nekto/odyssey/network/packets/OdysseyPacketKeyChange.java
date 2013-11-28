package nekto.odyssey.network.packets;

import java.io.DataInputStream;
import java.io.IOException;

import nekto.odyssey.craft.CraftManager;
import net.minecraft.entity.player.EntityPlayer;

public class OdysseyPacketKeyChange extends OdysseyPacket
{
	@Override
	public void handle(DataInputStream iStream, EntityPlayer player)
	{
		float deltaX = 0;
		float deltaY = 0;
		float deltaZ = 0;
		
		float deltaYaw = 0;
		
		try
		{
			deltaX = iStream.readFloat();
			deltaY = iStream.readFloat();
			deltaZ = iStream.readFloat();
			deltaYaw = iStream.readFloat();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		CraftManager.setSpeeds(deltaX, deltaY, deltaZ, deltaYaw);
	}
	
}
