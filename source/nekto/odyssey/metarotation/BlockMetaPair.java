package nekto.odyssey.metarotation;

public class BlockMetaPair implements Comparable<BlockMetaPair>
{
	public int id;
	public int meta;
	private static int lastId;

	public BlockMetaPair(int id)
	{
		this.id = id;
		meta = -1;
		lastId = id;
	}

	public BlockMetaPair(int id, int meta)
	{
		this.id = id;
		this.meta = meta;
		lastId = id;
	}

	public BlockMetaPair(String s)
	{
		String parts[] = s.split(":", 2);
		if (parts[0].trim().length() < 1)
		{
			id = lastId;
		} else
		{
			id = Integer.parseInt(parts[0].trim());
		}
		if (parts.length == 1)
		{
			meta = -1;
		} else
		{
			meta = Integer.parseInt(parts[1].trim());
		}
		lastId = id;
	}

	@Override
	public BlockMetaPair clone()
	{
		return new BlockMetaPair(id, meta);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof BlockMetaPair)
		{
			return id == ((BlockMetaPair) obj).id
					&& meta == ((BlockMetaPair) obj).meta;
		} else
		{
			return true;
		}
	}

	@Override
	public String toString()
	{
		if (meta != -1)
			return "" + id + ":" + meta;
		else
			return "" + id + ":*";
	}

	@Override
	public int hashCode()
	{
		return id * 256 + meta;
	}

	@Override
	public int compareTo(BlockMetaPair otherPair)
	{
		if (id < otherPair.id)
			return -1;
		if (id > otherPair.id)
			return 1;
		if (meta < otherPair.meta)
			return -1;
		if (meta > otherPair.meta)
			return 1;
		return 0;
	}
}
