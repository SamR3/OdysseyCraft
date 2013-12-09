package nekto.odyssey.metarotation;

import java.util.List;
import java.util.ArrayList;

import net.minecraft.src.ModLoader;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class RotatedBB extends AxisAlignedBB
{

	private static List<RotatedBB> rotatedBoundingBoxes = new ArrayList<RotatedBB>();
	private static int numRotatedBoundingBoxesInUse = 0;
	public double rotation = 0;

	private RotatedBB(double minX, double minY, double minZ, double maxX,
			double maxY, double maxZ, double rot)
	{
		super(minX, minY, minZ, maxX, maxY, maxZ);
		rotation = rot;
	}

	public static RotatedBB getBoundingBox(double minX, double minY,
			double minZ, double maxX, double maxY, double maxZ, double rot)
	{
		return new RotatedBB(minX, minY, minZ, maxX, maxY, maxZ, rot);
	}

	public static RotatedBB getBoundingBoxFromPool(double minX, double minY,
			double minZ, double maxX, double maxY, double maxZ)
	{
		return getBoundingBoxFromPool(minX, minY, minZ, maxX, maxY, maxZ, 0);
	}

	public static RotatedBB getBoundingBoxFromPool(double minX, double minY,
			double minZ, double maxX, double maxY, double maxZ, double rot)
	{
		return new RotatedBB(minX, minY, minZ, maxX, maxY, maxZ, rot);
	}

	public static RotatedBB getBoundingBoxFromPool(AxisAlignedBB box)
	{
		if (box instanceof RotatedBB)
		{
			return getBoundingBoxFromPool(box.minX, box.minY, box.minZ,
					box.maxX, box.maxY, box.maxZ, ((RotatedBB) box).rotation);
		} else
		{
			return getBoundingBoxFromPool(box.minX, box.minY, box.minZ,
					box.maxX, box.maxY, box.maxZ, 0);
		}
	}

	public static RotatedBB getBoundingBoxFromPool(AxisAlignedBB box, double rot)
	{
		return getBoundingBoxFromPool(box.minX, box.minY, box.minZ, box.maxX,
				box.maxY, box.maxZ, rot);
	}

	public static void clearBoundingBoxes()
	{
		// AxisAlignedBB.clearBoundingBoxes();
		rotatedBoundingBoxes.clear();
		numRotatedBoundingBoxesInUse = 0;
	}

	public static void clearBoundingBoxPool()
	{
		// AxisAlignedBB.clearBoundingBoxPool();
		numRotatedBoundingBoxesInUse = 0;
	}

	@Override
	public RotatedBB setBounds(double d, double d1, double d2, double d3,
			double d4, double d5)
	{
		minX = d;
		minY = d1;
		minZ = d2;
		maxX = d3;
		maxY = d4;
		maxZ = d5;
		return this;
	}

	public RotatedBB setRotation(double rot)
	{
		rotation = rot;
		return this;
	}

	@Override
	public RotatedBB addCoord(double d, double d1, double d2)
	{
		// TODO: Method stub
		return expand(Math.abs(d), Math.abs(d1), Math.abs(d2));
	}

	@Override
	public RotatedBB expand(double d, double d1, double d2)
	{
		// TODO: Method stub
		double d3 = Math.max(d, d2) * 1.4142135623730950488016887242097; // sqrt(2)
		return getBoundingBoxFromPool(minX - d3, minY - d1, minZ - d3, maxX
				+ d3, maxY + d1, maxZ + d3, rotation);
	}

	@Override
	protected boolean isVecInYZ(Vec3 vec3d)
	{
		if (vec3d == null)
		{
			return false;
		} else
		{
			AxisAlignedBB box = getExpandedAABB();
			return vec3d.yCoord >= minY && vec3d.yCoord <= maxY
					&& vec3d.zCoord >= box.minZ && vec3d.zCoord <= box.maxZ;
		}
	}

	@Override
	protected boolean isVecInXZ(Vec3 vec3d)
	{
		if (vec3d == null)
		{
			return false;
		} else
		{
			AxisAlignedBB box = this.copy();
			box.minY = -1;
			box.maxY = 1;
			Vec3 vec = Vec3.createVectorHelper(vec3d.xCoord, 0, vec3d.zCoord);
			return box.isVecInside(vec);
		}
	}

	@Override
	protected boolean isVecInXY(Vec3 vec3d)
	{
		if (vec3d == null)
		{
			return false;
		} else
		{
			AxisAlignedBB box = getExpandedAABB();
			return vec3d.xCoord >= box.minX && vec3d.xCoord <= box.maxX
					&& vec3d.yCoord >= minY && vec3d.yCoord <= maxY;
		}
	}

	@Override
	public RotatedBB getOffsetBoundingBox(double d, double d1, double d2)
	{
		return getBoundingBoxFromPool(minX + d, minY + d1, minZ + d2, maxX + d,
				maxY + d1, maxZ + d2, rotation);
	}

	@Override
	public double calculateXOffset(AxisAlignedBB box, double d)
	{
		return calculateXZOffset(box, Vec3.createVectorHelper(d, 0, 0)).xCoord;
	}

	@Override
	public double calculateYOffset(AxisAlignedBB axisalignedbb, double d)
	{
		// Project both boxes onto XZ plane.
		AxisAlignedBB box1 = this.copy();
		box1.minY = -1;
		box1.maxY = 1;
		AxisAlignedBB box2 = axisalignedbb.copy();
		box2.minY = -1;
		box2.maxY = 1;

		if (!box1.intersectsWith(box2))
		{
			// Projections don't intersect.
			return d;
		}
		// Projections intersect.

		// Rest of the code was copied from AxisAlignedBB.
		if (d > 0.0D && axisalignedbb.maxY <= minY)
		{
			double d1 = minY - axisalignedbb.maxY;
			if (d1 < d)
			{
				d = d1;
			}
		}
		if (d < 0.0D && axisalignedbb.minY >= maxY)
		{
			double d2 = maxY - axisalignedbb.minY;
			if (d2 > d)
			{
				d = d2;
			}
		}
		return d;
	}

	@Override
	public double calculateZOffset(AxisAlignedBB box, double d)
	{
		return calculateXZOffset(box, Vec3.createVectorHelper(0, 0, d)).zCoord;
	}

	private Vec3 calculateXZOffset(AxisAlignedBB axisalignedbb, Vec3 vecOffset)
	{
		if (minY >= axisalignedbb.maxY || maxY <= axisalignedbb.minY)
		{
			// Bounding boxes' projections onto Y-axis don't intersect.
			return vecOffset;
		}

		if (this.intersectsWith(axisalignedbb))
		{
			// Bounding boxes intersect.
			return vecOffset;
		}

		// Project everything on XZ plane.
		RotatedBB box1 = this.copy();
		box1.minY = -1;
		box1.maxY = 1;
		RotatedBB box2 = getBoundingBoxFromPool(axisalignedbb);
		box2.minY = -1;
		box2.maxY = 1;

		// if (box1.maxZ ) {
		//
		// }

		// Calculate corners.
		Vec3 vecA = rotateVec3AroundCenter(
				Vec3.createVectorHelper(box1.minX, 0, box1.minZ), box1.rotation);
		Vec3 vecB = rotateVec3AroundCenter(
				Vec3.createVectorHelper(box1.minX, 0, box1.maxZ), box1.rotation);
		Vec3 vecC = rotateVec3AroundCenter(
				Vec3.createVectorHelper(box1.maxX, 0, box1.maxZ), box1.rotation);
		Vec3 vecD = rotateVec3AroundCenter(
				Vec3.createVectorHelper(box1.maxX, 0, box1.minZ), box1.rotation);
		Vec3 vecK = rotateVec3AroundCenter(
				Vec3.createVectorHelper(box2.minX, 0, box2.minZ), box2.rotation);
		Vec3 vecL = rotateVec3AroundCenter(
				Vec3.createVectorHelper(box2.minX, 0, box2.maxZ), box2.rotation);
		Vec3 vecM = rotateVec3AroundCenter(
				Vec3.createVectorHelper(box2.maxX, 0, box2.maxZ), box2.rotation);
		Vec3 vecN = rotateVec3AroundCenter(
				Vec3.createVectorHelper(box2.maxX, 0, box2.minZ), box2.rotation);

		// Offset corners.
		Vec3 vecA1 = vecA.addVector(-vecOffset.xCoord, 0, -vecOffset.zCoord);
		Vec3 vecB1 = vecB.addVector(-vecOffset.xCoord, 0, -vecOffset.zCoord);
		Vec3 vecC1 = vecC.addVector(-vecOffset.xCoord, 0, -vecOffset.zCoord);
		Vec3 vecD1 = vecD.addVector(-vecOffset.xCoord, 0, -vecOffset.zCoord);
		Vec3 vecK1 = vecK.addVector(vecOffset.xCoord, 0, vecOffset.zCoord);
		Vec3 vecL1 = vecL.addVector(vecOffset.xCoord, 0, vecOffset.zCoord);
		Vec3 vecM1 = vecM.addVector(vecOffset.xCoord, 0, vecOffset.zCoord);
		Vec3 vecN1 = vecN.addVector(vecOffset.xCoord, 0, vecOffset.zCoord);

		// Expand bounding boxes a bit
		box1 = box1.expand(0.01, 0.01, 0.01);
		box2 = box2.expand(0.01, 0.01, 0.01);

		// Calculate intersections
		ArrayList<Vec3> intersections = new ArrayList<Vec3>();
		MovingObjectPosition mop;

		mop = box2.calculateIntercept(vecA, vecA1);
		if (mop != null && mop.hitVec != null)
		{
			intersections.add(vecA.subtract(mop.hitVec));
		}

		mop = box2.calculateIntercept(vecB, vecB1);
		if (mop != null && mop.hitVec != null)
		{
			intersections.add(vecB.subtract(mop.hitVec));
		}

		mop = box2.calculateIntercept(vecC, vecC1);
		if (mop != null && mop.hitVec != null)
		{
			intersections.add(vecC.subtract(mop.hitVec));
		}

		mop = box2.calculateIntercept(vecD, vecD1);
		if (mop != null && mop.hitVec != null)
		{
			intersections.add(vecD.subtract(mop.hitVec));
		}

		mop = box1.calculateIntercept(vecK, vecK1);
		if (mop != null && mop.hitVec != null)
		{
			intersections.add(mop.hitVec.subtract(vecK));
		}

		mop = box1.calculateIntercept(vecL, vecL1);
		if (mop != null && mop.hitVec != null)
		{
			intersections.add(mop.hitVec.subtract(vecL));
		}

		mop = box1.calculateIntercept(vecM, vecM1);
		if (mop != null && mop.hitVec != null)
		{
			intersections.add(mop.hitVec.subtract(vecM));
		}

		mop = box1.calculateIntercept(vecN, vecN1);
		if (mop != null && mop.hitVec != null)
		{
			intersections.add(mop.hitVec.subtract(vecN));
		}

		for (Vec3 intersection : intersections)
		{
			if (vecOffset.xCoord > 0)
			{
				vecOffset.xCoord = Math.min(vecOffset.xCoord,
						Math.max(intersection.xCoord, 0));
			}
			if (vecOffset.xCoord < 0)
			{
				vecOffset.xCoord = Math.max(vecOffset.xCoord,
						Math.min(intersection.xCoord, 0));
			}
			if (vecOffset.zCoord > 0)
			{
				vecOffset.zCoord = Math.min(vecOffset.zCoord,
						Math.max(intersection.zCoord, 0));
			}
			if (vecOffset.zCoord < 0)
			{
				vecOffset.zCoord = Math.max(vecOffset.zCoord,
						Math.min(intersection.zCoord, 0));
			}
		}

		return vecOffset;
	}

	@Override
	public boolean intersectsWith(AxisAlignedBB axisalignedbb)
	{
		if (minY >= axisalignedbb.maxY || maxY <= axisalignedbb.minY)
		{
			// Bounding boxes' projections onto Y-axis don't intersect.
			return false;
		}

		// Check if their projections onto XZ-plane intersect.
		AxisAlignedBB box1 = this.copy();
		box1.minY = -1;
		box1.maxY = 1;
		// box1 = box1.expand(0.01, 0.01, 0.01);
		AxisAlignedBB box2 = axisalignedbb.copy();
		box2.minY = -1;
		box2.maxY = 1;
		// box2 = box2.expand(0.01, 0.01, 0.01);

		// Get corners of projection of this box.
		Vec3 a = rotateVec3AroundCenter(Vec3.createVectorHelper(minX, 0, minZ),
				rotation);
		Vec3 b = rotateVec3AroundCenter(Vec3.createVectorHelper(minX, 0, maxZ),
				rotation);
		Vec3 c = rotateVec3AroundCenter(Vec3.createVectorHelper(maxX, 0, maxZ),
				rotation);
		Vec3 d = rotateVec3AroundCenter(Vec3.createVectorHelper(maxX, 0, minZ),
				rotation);

		// Intersect segments AB, BC, CD and DA with box1.
		if (box2.calculateIntercept(a, b) != null
				|| box2.calculateIntercept(b, c) != null
				|| box2.calculateIntercept(c, d) != null
				|| box2.calculateIntercept(d, a) != null)
		{
			return true;
		}

		// Either boxes don't intersect, or one is completely inside another.
		if (box1.isVecInside(Vec3.createVectorHelper(
				(box2.minX + box2.maxX) / 2, 0, (box2.minZ + box2.maxZ) / 2)))
		{
			return true;
		}
		if (box2.isVecInside(Vec3.createVectorHelper(
				(box1.minX + box1.maxX) / 2, 0, (box1.minZ + box1.maxZ) / 2)))
		{
			return true;
		}
		return false;
	}

	@Override
	public RotatedBB offset(double d, double d1, double d2)
	{
		minX += d;
		minY += d1;
		minZ += d2;
		maxX += d;
		maxY += d1;
		maxZ += d2;
		return this;
	}

	@Override
	public boolean isVecInside(Vec3 vec3d)
	{
		vec3d = rotateVec3AroundCenter(vec3d, -rotation);

		return super.isVecInside(vec3d);
	}

	@Override
	public double getAverageEdgeLength()
	{
		double d = maxX - minX;
		double d1 = maxY - minY;
		double d2 = maxZ - minZ;
		return (d + d1 + d2) / 3D;
	}

	@Override
	public RotatedBB contract(double d, double d1, double d2)
	{
		return expand(-d, -d1, -d2);
	}

	@Override
	public RotatedBB copy()
	{
		return getBoundingBoxFromPool(minX, minY, minZ, maxX, maxY, maxZ,
				rotation);
	}

	@Override
	public MovingObjectPosition calculateIntercept(Vec3 vec3d, Vec3 vec3d1)
	{
		vec3d = rotateVec3AroundCenter(vec3d, -rotation);
		vec3d1 = rotateVec3AroundCenter(vec3d1, -rotation);

		MovingObjectPosition mpo = super.calculateIntercept(vec3d, vec3d1);
		if (mpo != null && mpo.hitVec != null)
		{
			mpo.hitVec = rotateVec3AroundCenter(mpo.hitVec, rotation);
		}

		return mpo;
	}

	@Override
	public void setBB(AxisAlignedBB axisalignedbb)
	{
		minX = axisalignedbb.minX;
		minY = axisalignedbb.minY;
		minZ = axisalignedbb.minZ;
		maxX = axisalignedbb.maxX;
		maxY = axisalignedbb.maxY;
		maxZ = axisalignedbb.maxZ;
		if (axisalignedbb instanceof RotatedBB)
		{
			rotation = ((RotatedBB) axisalignedbb).rotation;
		} else
		{
			rotation = 0;
		}
	}

	@Override
	public String toString()
	{
		return (new StringBuilder()).append("box[").append(minX).append(", ")
				.append(minY).append(", ").append(minZ).append(" -> ")
				.append(maxX).append(", ").append(maxY).append(", ")
				.append(maxZ).append(" -> ").append(rotation).append("]")
				.toString();
	}

	public Vec3 rotateVec3AroundVec3(Vec3 vec, Vec3 vec2, double rot)
	{
		double cos = MathHelper.cos((float) (-rot / 180 * (Math.PI)));
		double sin = MathHelper.sin((float) (-rot / 180 * (Math.PI)));

		double relX = vec.xCoord - vec2.xCoord;
		double relZ = vec.zCoord - vec2.zCoord;

		double newRelX = relX * cos - relZ * sin;
		double newRelZ = relX * sin + relZ * cos;

		double newX = newRelX + vec2.xCoord;
		double newZ = newRelZ + vec2.zCoord;

		return Vec3.createVectorHelper(newX, vec.yCoord, newZ);
	}

	public Vec3 rotateVec3AroundCenter(Vec3 vec, double rot)
	{
		Vec3 center = Vec3.createVectorHelper((minX + maxX) / 2,
				(minY + maxY) / 2, (minZ + maxZ) / 2);
		return rotateVec3AroundVec3(vec, center, rot);
	}

	public RotatedBB rotateAroundVec3(Vec3 vec, double rot)
	{
		Vec3 oldCenter = Vec3.createVectorHelper((minX + maxX) / 2,
				(minY + maxY) / 2, (minZ + maxZ) / 2);
		Vec3 newCenter = rotateVec3AroundVec3(oldCenter, vec, rot);
		minX += (newCenter.xCoord - oldCenter.xCoord);
		maxX += (newCenter.xCoord - oldCenter.xCoord);
		minZ += (newCenter.zCoord - oldCenter.zCoord);
		maxZ += (newCenter.zCoord - oldCenter.zCoord);
		rotation += rot;
		return this;
	}

	public AxisAlignedBB getExpandedAABB()
	{
		Vec3 a = rotateVec3AroundCenter(
				Vec3.createVectorHelper(minX, minY, minZ), rotation);
		Vec3 b = rotateVec3AroundCenter(
				Vec3.createVectorHelper(minX, minY, maxZ), rotation);
		Vec3 c = rotateVec3AroundCenter(
				Vec3.createVectorHelper(maxX, minY, maxZ), rotation);
		Vec3 d = rotateVec3AroundCenter(
				Vec3.createVectorHelper(maxX, minY, minZ), rotation);

		double expandedMinX = Math.min(a.xCoord,
				Math.min(b.xCoord, Math.min(c.xCoord, d.xCoord)));
		double expandedMaxX = Math.max(a.xCoord,
				Math.max(b.xCoord, Math.max(c.xCoord, d.xCoord)));
		double expandedMinZ = Math.min(a.zCoord,
				Math.min(b.zCoord, Math.min(c.zCoord, d.zCoord)));
		double expandedMaxZ = Math.max(a.zCoord,
				Math.max(b.zCoord, Math.max(c.zCoord, d.zCoord)));

		return super.getBoundingBox(expandedMinX, minY, expandedMinZ,
				expandedMaxX, maxY, expandedMaxZ);
	}

}
