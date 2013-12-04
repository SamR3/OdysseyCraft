package nekto.odyssey.world;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import nekto.odyssey.entity.EntityBlock;
import nekto.odyssey.entity.EntityBlockConsole;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.logging.LogAgent;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.EnumGameType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGrid extends World implements IBlockAccess 
{
	public static boolean dynamicLighting = false;
	
	public World worldObj;
	
	public HashSet<EntityBlock> gridBlocks = new HashSet<EntityBlock>();
	public HashSet<EntityBlock> collidableGridBlocks = new HashSet<EntityBlock>();
	private HashMap<CoordinateWrapper, EntityBlock> gridBlockLookup = new HashMap<CoordinateWrapper, EntityBlock>(); 
	private HashMap<CoordinateWrapper, Integer> skylightMap = new HashMap<CoordinateWrapper, Integer>();
	private HashMap<CoordinateWrapper, Integer> blocklightMap = new HashMap<CoordinateWrapper, Integer>();
	
	private int[][][] blockIdLookup = new int[1][1][1]; //(Y X Z ORDERING)
	private int[][] blockHeightMap = new int[1][1]; // (X Z ORDERING)
	
	public ArrayList<Entity> entities = new ArrayList<Entity>();
	
	public RenderBlocks renderBlocks;
	
	private int[] lightUpdateBlockList = new int[32768];
	private boolean lightUpdatesAllowed = false;
	
	public EntityBlockConsole ref = null;
	
	public WorldGrid(EntityBlockConsole ref)
	{
		super(new FakeSaveHandler(), "Fake", new FakeWorldProvider(), new WorldSettings(0, EnumGameType.NOT_SET, false, false, WorldType.DEFAULT), new Profiler(), new LogAgent("Odyssey-Client", " [ODYSSEY]", (new File("output-odyssey-client.log")).getAbsolutePath()));
		this.renderBlocks = new RenderBlocks(this);
		this.ref = ref;
		ref.setGrid(this);
		this.worldObj = ref.worldObj;
		this.worldInfo = worldObj.getWorldInfo();
		this.rand = worldObj.rand;
		addBlock(ref);
	}
	
	@Override
	protected IChunkProvider createChunkProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity getEntityByID(int i) {
		// TODO Auto-generated method stub
		return null;
	}

}
