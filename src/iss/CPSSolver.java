package iss;

public class CPSSolver {
	public static final double sarjMaxVelocity = 0.15;

	public static final double sarjMaxAcceleration = 0.005;

	public static final boolean DEBUG = true;

	// TODO Need to Test
	public static double[] solveSarj(double[] rotations, double initial) {

		Range ranges[] = new Range[rotations.length + 1];
		ranges[0] = new Range(initial, initial);
		ranges[rotations.length] = new Range(initial, initial);
		boolean ok = true;

		// forward check
		for (int i = 1; i < rotations.length; i++) {
			ranges[i] = step(ranges[i - 1], rotations[i - 1], rotations[i]);
			System.out.println(ranges[i]);
			if (ranges[i] == null || ranges[i].isEmpty()) {
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
			if (backwardRange == null || backwardRange.isEmpty()) {
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
		for (int i = 0; i < rotations.length; i++) {
			res[i] = ranges[i].getMidValue();
		}
		return res;
	}

	public static Range step(Range velocityRange, double angle1, double angle2) {
		double maxNextV, minNextV;
		double s = ISSUtils.determineShift(angle1, angle2);

		System.out.println(" ##angle:" + angle1 + "-" + angle2 + "=" + s);
		{
			// 下がってから上がるパターン
			// 初期値スピードで、これ以上小さくなると解がなくなる境界値=どんなにがんばってもたどり着かない
			double lowlimitv = (s - 9.0) / 60.0;
			if (lowlimitv > velocityRange.max) {
				return null;
			}
			double minv = Math.max(velocityRange.min, lowlimitv);
			double r2 = 400.0 * (s - 60.0 * minv + 9.0);
			if (r2 < 0) {
				return null;
			}
			double t1 = 60 - Math.sqrt(r2 / 2.0);
			// vの速度拘束条件による制約
			double t1_limit = (minv + sarjMaxVelocity) * 200.0;
			if (DEBUG) {
				System.out.println(" *minv=" + minv + ",t1=" + t1
						+ ",t1_limit=" + t1_limit);
			}
			t1 = Math.min(t1, t1_limit);
			double core = r2 - ((t1 - 60) * (t1 - 60));
			if (core < 0) {
				return null;
			}
			double t2 = 60 - Math.sqrt(core);
			maxNextV = minv + (60.0 - t1 - t2) * sarjMaxAcceleration;
			if (DEBUG) {
				System.out.println(" => #(t1=" + t1 + ", t2=" + t2 + ", r^2="
						+ r2 + ")");
			}
		}
		{
			// 上がってから下がるパターン
			// これ以上大きくなると解がなくなる境界値=どんなにがんばってもたどり着かない
			double highlimitv = (s + 9.0) / 60.0;
			if (highlimitv < velocityRange.min) {
				return null;
			}
			double maxv = Math.min(velocityRange.max, highlimitv);
			double r2 = 400.0 * (-s + 60.0 * maxv + 9.0);
			if (r2 < 0) {
				return null;
			}
			double t1 = 60 - Math.sqrt(r2 / 2.0);
			// vの速度拘束条件による制約
			double t1_limit = (sarjMaxVelocity - maxv) * 200.0;
			t1 = Math.min(t1, t1_limit);
			double core = r2 - ((t1 - 60) * (t1 - 60));
			if (core < 0) {
				return null;
			}
			double t2 = 60 - Math.sqrt(core);
			minNextV = maxv + (t1 + t2 - 60.0) * sarjMaxAcceleration;
			if (DEBUG) {
				System.out.println(" => #(t1=" + t1 + ", t2=" + t2 + ", r^2="
						+ r2 + ")");
			}
		}
		// System.out.println("  => ###(" + minNextV + "," + maxNextV + ")");
		return new Range(Math.max(-sarjMaxVelocity, minNextV), Math.min(
				sarjMaxVelocity, maxNextV));
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
				return true;
			else
				return false;
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

		public String toString() {
			return "(" + min + "," + max + ")";
		}
	}

}
