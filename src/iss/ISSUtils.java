package iss;
public class ISSUtils {

    public static double minDegree(double r1, double r2) {
        double d = Math.abs(r1 - r2);
        if (d > 180.0)
            return 360 - d;
        else
            return d;
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
