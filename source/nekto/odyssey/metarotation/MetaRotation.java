package nekto.odyssey.metarotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;

public class MetaRotation
{
	public static HashMap<BlockMetaPair, BlockMetaPair> rotationDatabase = new HashMap<BlockMetaPair, BlockMetaPair>();
	public static File mcDir = Minecraft.getMinecraft().mcDataDir;
	public static File metaRotationDir = new File(mcDir,
			"/config/MetaRotation/");

	public static void addRotationData(BlockMetaPair oldbmp,
			BlockMetaPair newbmp)
	{
		rotationDatabase.put(oldbmp, newbmp);
	}

	public static boolean addRotationDataFromString(String s)
	{
		try
		{
			// Separate bitmask
			int bitmask = 15;
			String parts[] = s.split("bm:", 2);
			s = parts[0];
			try
			{
				bitmask = Integer.parseInt(parts[1].trim());
			} catch (Exception e)
			{

			}

			// Split BlockMetaPairs
			parts = s.split("->");
			BlockMetaPair abmp[] = new BlockMetaPair[parts.length];
			for (int i = 0; i < parts.length; i++)
			{
				abmp[i] = new BlockMetaPair(parts[i]);
			}

			// Add rotation data
			for (int i = 0; i < parts.length - 1; i++)
			{
				for (int j = 0; j < 15; j++)
				{
					if ((j & bitmask) == 0)
					{
						BlockMetaPair oldbmp = abmp[i].clone();
						BlockMetaPair newbmp = abmp[i + 1].clone();
						oldbmp.meta = oldbmp.meta & bitmask | j;
						newbmp.meta = newbmp.meta & bitmask | j;
						addRotationData(oldbmp, newbmp);
					}
				}
			}
			return true;
		} catch (Exception e)
		{
			return false;
		}
	}

	public static boolean addRotationDataFromFile(File file)
	{
		boolean flag = true;

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null)
			{
				line = line.trim();
				if (line.length() < 1 || line.charAt(0) == ';')
				{
					// empty line or comment
				} else
				{
					flag &= addRotationDataFromString(line);
				}
			}

			br.close();
		} catch (Exception e)
		{
			return false;
		}
		
		return flag;
	}

	public static boolean addRotationDataFromFolder(File folder)
	{
		boolean flag = true;
		for (File file : folder.listFiles())
		{
			if (file.isDirectory())
			{
				flag &= addRotationDataFromFolder(file);
			}
			if (file.getName().endsWith(".meta"))
			{
				flag &= addRotationDataFromFile(file);
			}
			/*
			 * if (file.getName().endsWith(".zip")) { ZipFile zip;
			 * 
			 * zip = new ZipFile(file); flag &= addRotationDataFromZip(zip); }
			 */
		}
		return flag;
	}

	public static BlockMetaPair rotateMeta(int id, int meta, int rotationAmount)
	{
		return rotateMeta(new BlockMetaPair(id, meta), rotationAmount);
	}

	public static BlockMetaPair rotateMeta(Block block, int meta,
			int rotationAmount)
	{
		return rotateMeta(new BlockMetaPair(block.blockID, meta),
				rotationAmount);
	}

	public static BlockMetaPair rotateMeta(String s, int rotationAmount)
	{
		return rotateMeta(new BlockMetaPair(s), rotationAmount);
	}

	public static BlockMetaPair rotateMeta(BlockMetaPair originalIDMeta,
			int rotationAmount)
	{
		// printDatabase();
		try
		{
			rotationAmount %= 4;
			BlockMetaPair newIDMeta = originalIDMeta;

			for (int i = 0; i < rotationAmount; i++)
			{
				newIDMeta = rotateMetaOnce(newIDMeta);
			}

			if (newIDMeta == null)
				newIDMeta = originalIDMeta;

			return newIDMeta;
		} catch (Exception e)
		{
			return originalIDMeta;
		}
	}

	private static BlockMetaPair rotateMetaOnce(BlockMetaPair originalIDMeta)
	{
		BlockMetaPair newIDMeta = rotationDatabase.get(originalIDMeta);

		if (newIDMeta == null)
			newIDMeta = originalIDMeta;

		return newIDMeta;
	}

	public static void createDefaultFile()
	{
		try
		{
			if (!metaRotationDir.exists())
			{
				metaRotationDir.mkdir();
			}

			File vanillaMeta = new File(metaRotationDir, "/Vanilla.meta");

			if (!vanillaMeta.exists())
			{
				String s = "";
				s += "; This file must be located in .minecraft/config/MetaRotation, or any subfolder.\n";
				s += ";\n";
				s += ";\n";
				s += ";\n";
				s += "; Lines starting with ; are comments.\n";
				s += ";\n";
				s += "; Each line has format\n";
				s += "; <id1>:<meta1> -> [<id2>]:<meta2> -> ... -> [<idn>]:<metan> [bm: <bitmask>]\n";
				s += "; This means that when rotating counterclockwise once, block id1:meta1 will turn\n";
				s += "; into block id2:meta2; id2:meta2 will turn into id3:meta3; etc.\n";
				s += "; If you don't specify id, previous id will be used.\n";
				s += "; Usually idn = id1 and metan = meta1\n";
				s += ";\n";
				s += "; <bitmask> is an optional bitmask.";
				s += "; Bitmask just makes your code smaller.\n";
				s += "; For example\n";
				s += "; 26:3 -> :2 -> :1 -> :0 -> :3 bm: 3\n";
				s += "; is the shortcut for\n";
				s += "; 26: 3 -> : 2 -> : 1 -> : 0 -> : 3\n";
				s += "; 26: 7 -> : 6 -> : 5 -> : 4 -> : 7\n";
				s += "; 26:11 -> :10 -> : 9 -> : 8 -> :11\n";
				s += "; 26:15 -> :14 -> :13 -> :12 -> :15\n";
				s += ";\n";
				s += ";----------------------------------------------\n";
				s += ";\n";
				s += "\n";
				s += "\n";
				s += "\n";
				s += "; Bed.\n";
				s += "26:3 -> :2 -> :1 -> :0 -> :3 bm: 3\n";
				s += "\n";
				s += "; Pistons.\n";
				s += "29:2 -> :4 -> :3 -> :5 -> :2 bm: 7\n";
				s += "33:2 -> :4 -> :3 -> :5 -> :2 bm: 7\n";
				s += "34:2 -> :4 -> :3 -> :5 -> :2 bm: 7\n";
				s += "\n";
				s += "; Torches.\n";
				s += "50:4 -> :2 -> :3 -> :1 -> :4\n";
				s += "75:4 -> :2 -> :3 -> :1 -> :4\n";
				s += "76:4 -> :2 -> :3 -> :1 -> :4\n";
				s += "\n";
				s += "; Stairs.\n";
				s += " 53:3 -> :1 -> :2 -> :0 -> :3bm: 3\n";
				s += " 67:3 -> :1 -> :2 -> :0 -> :3bm: 3\n";
				s += "108:3 -> :1 -> :2 -> :0 -> :3bm: 3\n";
				s += "109:3 -> :1 -> :2 -> :0 -> :3bm: 3\n";
				s += "114:3 -> :1 -> :2 -> :0 -> :3bm: 3\n";
				s += "\n";
				s += "; Dispenser. Chest. Furnaces.\n";
				s += "23:2 -> :4 -> :3 -> :5 -> :2\n";
				s += "54:2 -> :4 -> :3 -> :5 -> :2\n";
				s += "61:2 -> :4 -> :3 -> :5 -> :2\n";
				s += "62:2 -> :4 -> :3 -> :5 -> :2\n";
				s += "\n";
				s += "; Signs.\n";
				s += "63:12 -> :8 -> :4 -> :0 -> :12 bm: 12\n";
				s += "68:2 -> :4 -> :3 -> :5 -> :2\n";
				s += "\n";
				s += "; Doors. Trapdoor. Gate.\n";
				s += " 64:3 -> :2 -> :1 -> :0 -> :3 bm: 3\n";
				s += " 71:3 -> :2 -> :1 -> :0 -> :3 bm: 3\n";
				s += " 96:0 -> :2 -> :1 -> :3 -> :0 bm: 3\n";
				s += "107:3 -> :2 -> :1 -> :0 -> :3 bm: 3\n";
				s += "\n";
				s += "; Ladder.\n";
				s += "65:2 -> :4 -> :3 -> :5 -> :2\n";
				s += "\n";
				s += "; Lever.\n";
				s += "69:4 -> :2 -> :3 -> :1 -> :4 bm: 7\n";
				s += "\n";
				s += "; Button.\n";
				s += "77:4 -> :2 -> :3 -> :1 -> :4 bm: 7\n";
				s += "\n";
				s += "; Pumpkins.\n";
				s += "86:3 -> :2 -> :1 -> :0 -> 86:3\n";
				s += "91:3 -> :2 -> :1 -> :0 -> 91:3\n";
				s += "\n";
				s += "; Repeaters.\n";
				s += "93:3 -> :2 -> :1 -> :0 -> :3 bm: 3\n";
				s += "94:3 -> :2 -> :1 -> :0 -> :3 bm: 3\n";
				s += "\n";
				s += "; Rails.\n";
				s += "    ; Straight\n";
				s += "    27:0 -> :1 -> :0 bm: 7\n";
				s += "    28:0 -> :1 -> :0\n";
				s += "    66:0 -> :1 -> :0\n";
				s += "    ; Slopes\n";
				s += "    27:2 -> :4 -> :3 -> :5 -> :2 bm: 7\n";
				s += "    28:2 -> :4 -> :3 -> :5 -> :2\n";
				s += "    66:2 -> :4 -> :3 -> :5 -> :2\n";
				s += "    ; Corners\n";
				s += "    66:9 -> :8 -> :7 -> :6 -> :9\n";
				s += "\n";
				s += "; Vines.\n";
				s += "106: 8 -> : 4 -> : 2 -> : 1 -> :8\n";
				s += "106: 9 -> :12 -> : 6 -> : 3 -> :9\n";
				s += "106:10 -> : 5 -> :10\n";
				s += "106: 7 -> :11 -> :13 -> :14 -> :7\n";
				s += "\n";
				s += "\n";
				s += ";Huge Mushrooms\n";
				s += "99:1 -> :7 -> :9 -> :3 -> :1\n";
				s += "99:2 -> :4 -> :8 -> :6 -> :2\n";
				s += "100:1 -> :7 -> :9 -> :3 -> :1\n";
				s += "100:2 -> :4 -> :8 -> :6 -> :2\n";

				vanillaMeta.createNewFile();
				FileWriter writer = new FileWriter(vanillaMeta);
				writer.write(s);
				writer.close();
			}
		} catch (Exception e)
		{
		}
	}
}
