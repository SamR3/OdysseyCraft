package nekto.odyssey.core;

import java.util.EnumSet;

import nekto.odyssey.network.PacketManager;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;

public class OdysseyKeyBind extends KeyHandler
{
	 private EnumSet<TickType> tickTypes = EnumSet.of(TickType.CLIENT);
	 private boolean keyPressed;
	 //private float yaw;
     
     public OdysseyKeyBind(KeyBinding[] KeyBindings, boolean[] repeatings)
     {
         super(KeyBindings, repeatings);
     }
     
     @Override
     public String getLabel()
     {
    	 return "Odyssey Controls";
     }
     
     @Override
     public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
     {
         if(!keyPressed)
         {
        	 keyPressed = true;
        	 
        	 float s1 = 0;
        	 float s2 = 0;
        	 float s3 = 0;
        	 
        	 switch(kb.keyCode)
        	 {
        		 case Keyboard.KEY_I:
        			 s2 = 0.1F;
        			 break;
        			 
        		 case Keyboard.KEY_K:
        			 s2 = -0.1F;
        			 break;
        			 
        		 case Keyboard.KEY_J:
        			 s1 = -0.1F;
        			 break;
        			 
        		 case Keyboard.KEY_L:
        			 s1 = 0.1F;
        			 break;
        			 
        	     default:
        	    	 break;
        	 }
        	 
        	 PacketDispatcher.sendPacketToServer(PacketManager.generateKeyPacket(s1, s2, s3, 0));
        	 System.out.println("Sent speed to server: " + s1 + ", " + s2 + ", " + s3);
         }
     }
         
     @Override
     public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
     {
         keyPressed = false;
     }
     
     @Override
     public EnumSet<TickType> ticks()
     {
         return tickTypes;
     }
}
