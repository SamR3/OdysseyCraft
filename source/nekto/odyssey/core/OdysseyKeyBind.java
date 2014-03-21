package nekto.odyssey.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.sun.java.util.jar.pack.Package.Class.Method;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;

public class OdysseyKeyBind// extends KeyHandler
{
	private static OdysseyKeyBind instance = new OdysseyKeyBind();
	private OdysseyKeyBind(){}
	private static HashMap<String, ZeppelinKeyBinding> keyBindMap = new HashMap<String, ZeppelinKeyBinding>();
	private static HashSet<ZeppelinKeyBinding> keyBinds = new HashSet<ZeppelinKeyBinding>();
	private static int keyRepeatRate = 100;
	
	public static void registerKeyBind(String keyBindDescription, String displayString, int keyCode)
	{
		ZeppelinKeyBinding kb = instance.new ZeppelinKeyBinding(keyBindDescription, keyCode);
		if (keyBinds.add(kb))
		{
			keyBindMap.put(keyBindDescription, kb);
		}
	}
	
	public static void setRepeatRate(int repeatRate)
	{
		keyRepeatRate = repeatRate;
	}
	
	public static boolean setKeyEventMethod(String keyBindDescription, Object obj, String methodName)
	{
		ZeppelinKeyBinding kb = keyBindMap.get(keyBindDescription);
		
		if (kb == null)
		{
			return false;
		}
		
		try 
		{
			kb.keyEventMethod = obj.getClass().getMethod(methodName);
			kb.keyEventObject = obj;
			return true;
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
			return false;
		} 
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public static void tick()
	{
		for(ZeppelinKeyBinding key : keyBinds)
		{
			if (key.doKeyPress())
			{
				try {
					key.keyEventMethod.invoke(key.keyEventObject);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}	
	}
	
	private class ZeppelinKeyBinding
	{
		/*
		 * The actual key binding object.
		 */
		private KeyBinding key;
		
		/*
		 * When the key was pressed.
		 */
		private long keyPressTime = 0;
		
		/*
		 * The last key state (updated by doKeyPress).
		 */
		private boolean wasKeyPressed = false;
		
		/*
		 * The method to run for this key.
		 */
		public Method keyEventMethod;
		
		/*
		 * The object to invoke the method on.
		 */
		public Object keyEventObject;
		
		public ZeppelinKeyBinding(String keyBindDescription, int keyCode)
		{
			this.key = new KeyBinding(keyBindDescription, keyCode);
		}
		
		/*
		 * Returns true if the key is currently pressed.
		 */
		public boolean isKeyPressed()
		{
			return(Keyboard.isKeyDown(key.keyCode));
		}
		
		/*
		 * Returns true if the keypress event should fire.
		 */
		public boolean doKeyPress()
		{
			boolean doPress = false;
			
			//Is the key down?
			if (isKeyPressed())
			{
				//Key is down, was it down last time?
				if (wasKeyPressed)
				{
					//It was down last time, see if it has been down for longer than the repeat threshold
					if (System.currentTimeMillis() > keyPressTime + OdysseyKeyBind.keyRepeatRate)
					{
						//Repeat threshold passed, reset timer and press key
						doPress = true;
						keyPressTime = System.currentTimeMillis();
					}
					else
					{
						//Key is pressed but threshold not passed, do not repeat.
						doPress = false;
					}
				}
				else
				{
					//Key has just been pressed.  Record time and trigger keypress.
					doPress = true;
					keyPressTime = System.currentTimeMillis();
				}

			}
			else 
			{
				//Key is not pressed, clear the last time.
				keyPressTime = 0;
				doPress = false;
			}
			
			//Record current keystate.
			wasKeyPressed = isKeyPressed();
			return doPress;
		}

		public KeyBinding getKeyBinding()
		{
			return key;
		}
		
		@Override
		public boolean equals(Object b)
		{
			return ((b instanceof ZeppelinKeyBinding) && this.getKeyBinding().keyDescription.equalsIgnoreCase(((ZeppelinKeyBinding)b).getKeyBinding().keyDescription));
		}
		
		@Override
		public int hashCode()
		{
			return this.getKeyBinding().keyDescription.hashCode();
		}
	}

	/*
	 * Registers the entire keybinding array with modloader.  
	 * DO NOT CALL MULTIPLE TIMES!
	 */
	public static void registerKeysWithModloader(OdysseyCore mod)
	{
		for (ZeppelinKeyBinding key : keyBinds)
		{
			KeyBindingRegistry.registerKeyBinding(key.key, false);
		}
		
	}
}
