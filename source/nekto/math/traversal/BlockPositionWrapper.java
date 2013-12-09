package nekto.math.traversal;

public class BlockPositionWrapper 
{
	public static boolean useDiagonal = false;
	
	public Integer x;
	public Integer y;
	public Integer z;
	
	public BlockPositionWrapper()
	{
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	
	public BlockPositionWrapper(int x, int y, int z)
	{
		this.x = Integer.valueOf(x);
		this.y = Integer.valueOf(y);
		this.z = Integer.valueOf(z);
	}
	
	public BlockPositionWrapper(int[] array)
	{
		this.x = array[0];
		this.y = array[1];
		this.z = array[2];
	}
	
	@Override
	public boolean equals(Object test)
	{
		return (test instanceof BlockPositionWrapper && this.x.equals(((BlockPositionWrapper)test).x) && this.y.equals(((BlockPositionWrapper)test).y) && this.z.equals(((BlockPositionWrapper)test).z));
	}
	
	@Override
	public String toString()
	{
		return "[ ".concat(this.x.toString()).concat(", ").concat(this.y.toString().concat(", ")).concat(this.z.toString()).concat("]");
	}
	
	@Override
	public int hashCode()
	{
		return this.toString().hashCode();
	}
}
