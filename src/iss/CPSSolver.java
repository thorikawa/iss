package iss;

public class CPSSolver {
	public static final double sarjMaxVelocity = 0.15;

	public static final double sarjMaxAcceleration = 0.005;

	// TODO Need to Test
	public static double[] solveSarj(double[] rotations, double initial) {

		Range ranges[] = new Range[rotations.length + 1];
		ranges[0] = new Range(initial, initial);
		ranges[rotations.length] = new Range(initial, initial);
		boolean ok = true;

		// forward check
		for (int i = 1; i < rotations.length; i++) {
			ranges[i] = step(ranges[i - 1], rotations[i - 1], rotations[i]);
			if (ranges[i].isEmpty()) {
				ok = false;
				break;
			}
		}
		if (!ok) {
			System.err.println("Forward check fail");
			return null;
		}
		// final check of forward check = cyclic check
		if (!ranges[rotations.length - 1].contains(initial)) {
			System.err.println("This is not cyclic");
			return null;
		}
		// backword check
		// first step
		for (int i = rotations.length; i >= 1; i--) {
			double srcRotation;
			if (i == rotations.length) {
				srcRotation = rotations[0];
			} else {
				srcRotation = rotations[i];
			}
			Range backwardRange = step(ranges[i], srcRotation, rotations[i - 1]);
			if (backwardRange.isEmpty()) {
				ok = false;
				break;
			}
			ranges[i - 1] = backwardRange.intersect(ranges[i - 1]);
			if (ranges[i - 1] == null || ranges[i - 1].isEmpty()) {
				ok = false;
				break;
			}
		}
		if (!ok) {
			System.err.println("Backward check fail");
			return null;
		}

		System.err.println("CSP solved");
		double res[] = new double[rotations.length];
		for (int i=0; i<rotations.length; i++) {
			res[i] = ranges[i].getMidValue();
		}
		return res;
	}

	public static Range step(Range velocityRange, double angle1, double angle2) {
		double maxNextV, minNextV;
		double s = ISSUtils.determineShift(angle1, angle2);
		{
			// (s - 9.0) / 60.0 : これ以上小さくなると解がなくなる境界値=どんなにがんばってもたどり着かない
			double minv = Math.max(velocityRange.min, (s - 9.0) / 60.0);
			double r2 = 400.0 * (s - 60.0 * minv + 9.0);
			double t1 = 60 - Math.sqrt(r2 / 2.0);
			// vの速度拘束条件による制約
			double t1_limit = minv + sarjMaxVelocity * 200.0;
			t1 = Math.min(t1, t1_limit);

			double t2 = 60 - Math.sqrt(r2 - (t1 * t1));
			maxNextV = minv + (60.0 - t1 - t2) * sarjMaxAcceleration;
		}
		{
			double maxv = Math.max(velocityRange.max, (s + 9.0) / 60.0);
			double r2 = 400.0 * (-s + 60.0 * maxv + 9.0);
			double t1 = 60 - Math.sqrt(r2 / 2.0);
			// vの速度拘束条件による制約
			double t1_limit = (sarjMaxVelocity - maxv) * 200.0;
			t1 = Math.min(t1, t1_limit);
			double t2 = 60 - Math.sqrt(r2 - (t1 * t1));
			minNextV = maxv + (t1 + t2 - 60.0) * sarjMaxAcceleration;
		}
		return new Range(Math.max(-sarjMaxVelocity, minNextV), Math.min(
				sarjMaxAcceleration, maxNextV));
	}

	public static class Range {
		public double min;

		public double max;

		public Range(double min, double max) {
			this.min = min;
			this.max = max;
		}

		public boolean isEmpty() {
			if (min > max)
				return false;
			else
				return true;
		}

		public boolean contains(double t) {
			if (t <= max && t >= min)
				return true;
			else
				return false;
		}

		/**
		 * return null if this or other is empty range. otherwise return Range
		 * object.
		 * 
		 * @param other
		 * @return
		 */
		public Range intersect(Range other) {
			if (this.isEmpty() || other.isEmpty())
				return null;
			double newMax = Math.min(this.max, other.max);
			double newMin = Math.max(this.min, other.min);
			return new Range(newMin, newMax);
		}

		public double getMidValue() {
			return (min + max) / 2.0;
		}
	}

}
