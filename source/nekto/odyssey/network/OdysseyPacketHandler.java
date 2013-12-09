package nekto.odyssey.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Hashtable;
import java.util.Map;

import nekto.odyssey.lib.GeneralRef;
import nekto.odyssey.network.packets.OdysseyPacket;
import nekto.odyssey.network.packets.OdysseyPacketBlockUpdate;
import nekto.odyssey.network.packets.OdysseyPacketKeyChange;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class OdysseyPacketHandler implements IPacketHandler
{
	private Map<String, OdysseyPacket> types = new Hashtable<String, OdysseyPacket>();

	OdysseyPacketBlockUpdate packetBlockUpdate = new OdysseyPacketBlockUpdate();
	OdysseyPacketKeyChange packetKeyChange = new OdysseyPacketKeyChange();

	public OdysseyPacketHandler()
	{
		types.put(GeneralRef.BLOCK_UPDATE_CHANNEL, packetBlockUpdate);
		types.put(GeneralRef.KEY_CHANGE_CHANNEL, packetKeyChange);
	}

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player par3Player)
	{
		EntityPlayer player = (EntityPlayer) par3Player;
		DataInputStream iStream = new DataInputStream(new ByteArrayInputStream(
				packet.data));

		types.get(packet.channel).handle(iStream, player);
	}
}
