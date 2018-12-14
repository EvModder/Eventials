package ParticleEffects;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class VectorUtils {

	private VectorUtils() {
	}

	public static final Vector rotateAroundAxisX(Vector v, float angle) {
		double y, z, cos, sin;
		cos = MathUtils.cos(angle);
		sin = MathUtils.sin(angle);
		y = v.getY() * cos - v.getZ() * sin;
		z = v.getY() * sin + v.getZ() * cos;
		return v.setY(y).setZ(z);
	}

	public static final Vector rotateAroundAxisY(Vector v, float angle) {
		double x, z, cos, sin;
		cos = MathUtils.cos(angle);
		sin = MathUtils.sin(angle);
		x = v.getX() * cos + v.getZ() * sin;
		z = v.getX() * -sin + v.getZ() * cos;
		return v.setX(x).setZ(z);
	}

	public static final Vector rotateAroundAxisZ(Vector v, float angle) {
		double x, y, cos, sin;
		cos = MathUtils.cos(angle);
		sin = MathUtils.sin(angle);
		x = v.getX() * cos - v.getY() * sin;
		y = v.getX() * sin + v.getY() * cos;
		return v.setX(x).setY(y);
	}

	public static final Vector rotateVector(Vector v, float angleX, float angleY, float angleZ) {
		// double x = v.getX(), y = v.getY(), z = v.getZ();
		// double cosX = ughcos(angleX), sinX = ughsin(angleX), cosY =
				// ughcos(angleY), sinY = ughsin(angleY), cosZ = ughcos(angleZ),
		// sinZ = ughsin(angleZ);
		// double nx, ny, nz;
		// nx = (x * cosY + z * sinY) * (x * cosZ - y * sinZ);
		// ny = (y * cosX - z * sinX) * (x * sinZ + y * cosZ);
		// nz = (y * sinX + z * cosX) * (-x * sinY + z * cosY);
		// return v.setX(nx).setY(ny).setZ(nz);
		// Having some strange behavior up there.. Have to look in it later. TODO
		rotateAroundAxisX(v, angleX);
		rotateAroundAxisY(v, angleY);
		rotateAroundAxisZ(v, angleZ);
		return v;
	}

	/**
	 * Rotate a vector about a location using that location's direction
	 *
	 * @param v
	 * @param location
	 * @return
	 */
	public static final Vector rotateVector(Vector v, Location location) {
		return rotateVector(v, location.getYaw(), location.getPitch());
	}

	/**
	 * This handles non-unit vectors, with yaw and pitch instead of X,Y,Z angles.
	 *
	 * Thanks to SexyToad!
	 *
	 * @param v
	 * @param yawDegrees
	 * @param pitchDegrees
	 * @return
	 */
	public static final Vector rotateVector(Vector v, float yawDegrees, float pitchDegrees) {
		float yaw = -MathUtils.degreesToRadians * (yawDegrees + 90);
		float pitch = -MathUtils.degreesToRadians * pitchDegrees;

		float cosYaw = MathUtils.cos(yaw);
		float cosPitch = MathUtils.cos(pitch);
		float sinYaw = MathUtils.sin(yaw);
		float sinPitch = MathUtils.sin(pitch);

		double initialX, initialY, initialZ;
		double x, y, z;

		// Z_Axis rotation (Pitch)
		initialX = v.getX();
		initialY = v.getY();
		x = initialX * cosPitch - initialY * sinPitch;
		y = initialX * sinPitch + initialY * cosPitch;

		// Y_Axis rotation (Yaw)
		initialZ = v.getZ();
		initialX = x;
		z = initialZ * cosYaw - initialX * sinYaw;
		x = initialZ * sinYaw + initialX * cosYaw;

		return new Vector(x, y, z);
	}

	public static final double angleToXAxis(Vector vector) {
		return MathUtils.atan2(vector.getX(), vector.getY());
	}
}