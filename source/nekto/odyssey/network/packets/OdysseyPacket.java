package nekto.odyssey.network.packets;

import java.io.DataInputStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;

public abstract class OdysseyPacket extends Packet250CustomPayload
{
	public abstract void handle(DataInputStream iStream, EntityPlayer player);
}
