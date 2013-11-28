package nekto.odyssey.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import nekto.odyssey.lib.GeneralRef;
import nekto.odyssey.network.packets.OdysseyPacketBlockUpdate;
import nekto.odyssey.network.packets.OdysseyPacketKeyChange;

public class PacketManager
{
	public static OdysseyPacketBlockUpdate generateUpdatePacket(int x, int y, int z)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try
		{
			dos.writeInt(x);
			dos.writeInt(y);
			dos.writeInt(z);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		OdysseyPacketBlockUpdate packet = new OdysseyPacketBlockUpdate();
		packet.channel = GeneralRef.BLOCK_UPDATE_CHANNEL;
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		
		return packet;
	}
	
	public static OdysseyPacketKeyChange generateKeyPacket(float x, float y, float z, float yaw)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try
		{
			dos.writeFloat(x);
			dos.writeFloat(y);
			dos.writeFloat(z);
			dos.writeFloat(yaw);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		OdysseyPacketKeyChange packet = new OdysseyPacketKeyChange();
		packet.channel = GeneralRef.KEY_CHANGE_CHANNEL;
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		
		return packet;
	}
}
