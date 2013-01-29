package iss;

public class ISSUtils {

	public static double minDegreeAbs(double r1, double r2) {
		double d = Math.abs(r1 - r2);
		if (d > 180.0)
			return 360 - d;
		else
			return d;
	}

	public static double determineShift(double angle1, double angle2) {
		double shift = angle2 - angle1;
		if (shift < -180.0)
			shift += 360.0;
		if (shift > 180.0)
			shift -= 360.0;
		return shift;
	}

	public static double normalizeDegree(double degree) {
		while (degree >= 360.0) {
			degree -= 360.0;
		}
		if (degree < 0) {
			return degree + 360.0;
		} else {
			return degree;
		}
	}
}
