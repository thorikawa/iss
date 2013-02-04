package iss;

public class VelocityChecker {

	private double maxVelocity = 0.15;

	private double maxAcceleration = 0.005;

	public static final double CHECK_DELTA = 0.01;

	public static boolean DEBUG = false;

	public VelocityChecker(double maxVelocity, double maxAccelaration) {
		this.maxVelocity = maxVelocity;
		this.maxAcceleration = maxAccelaration;
	}

	/**
	 * Find possible velocities array for given rotations. If there is no
	 * possible velocities, return null.
	 * 
	 * @param rotations
	 * @return
	 */
	public double[] findPossibleVelocities(double[] rotations) {
		for (double initial = 0.0; initial < maxVelocity; initial += CHECK_DELTA) {
			{
				double[] velocities = this.solveSarj(rotations, initial);
				if (velocities != null) {
					return velocities;
				}
			}
			if (initial != 0.0) {
				double[] velocities = this.solveSarj(rotations, -initial);
				if (velocities != null) {
					return velocities;
				}
			}
		}
		return null;
	}

	/**
	 * If there is no answer, return null. Otherwise return one possible answer.
	 * 
	 * @param rotations
	 * @param initial
	 * @return
	 */
	public double[] solveSarj(double[] rotations, double initial) {

		if (DEBUG) {
			System.out.println("===initial speed:" + initial);
		}
		Range ranges[] = new Range[rotations.length + 1];
		ranges[0] = new Range(initial, initial);
		ranges[rotations.length] = new Range(initial, initial);
		boolean ok = true;

		// forward check
		for (int i = 1; i < rotations.length; i++) {
			ranges[i] = step(ranges[i - 1], rotations[i - 1], rotations[i]);
			if (DEBUG) {
				System.out.println(ranges[i]);
			}
			if (ranges[i] == null || ranges[i].isEmpty()) {
				ok = false;
				break;
			}
		}
		if (!ok) {
			if (DEBUG) {
				System.out.println("Forward check fail");
			}
			return null;
		}
		// final check of forward check = cyclic check
		if (!ranges[rotations.length - 1].contains(initial)) {
			if (DEBUG) {
				System.out
						.println("[Warning] Forward check fail. This is not cyclic. It cannot end up with initial speed.");
			}
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
			// XXXXXXXXXXXXXXXXXXXX 第二引数と第三引数の順番怪しい・・・
			// Range backwardRange = step(ranges[i], srcRotation, rotations[i -
			// 1]);
			Range backwardRange = step(ranges[i], rotations[i - 1], srcRotation);
			if (backwardRange == null || backwardRange.isEmpty()) {
				ok = false;
				break;
			}
			if (DEBUG) {
				System.out.println("beforeIntersect:" + backwardRange);
			}
			ranges[i - 1] = backwardRange.intersect(ranges[i - 1]);
			if (DEBUG) {
				System.out.println("afterIntersect:" + ranges[i - 1]);
			}
			if (ranges[i - 1] == null || ranges[i - 1].isEmpty()) {
				ok = false;
				break;
			}
		}
		if (!ok) {
			if (DEBUG) {
				System.out.println("Backward check fail");
			}
			return null;
		}

		if (DEBUG) {
			System.out.println("CSP solved");
		}
		double res[] = new double[rotations.length];
		for (int i = 0; i < rotations.length; i++) {
			res[i] = ranges[i].getMidValue();
			// res[i] = ranges[i].getMinimumAbsoluteValue();
		}
		return res;
	}

	public Range step(Range velocityRange, double angle1, double angle2) {
		double maxNextV, minNextV;
		double s = ISSUtils.determineShift(angle1, angle2);

		if (DEBUG) {
			System.out.println(" ##angle:[" + angle1 + "]-[" + angle2 + "]="
					+ s);
			System.out.println(" ##srcrange:" + velocityRange);
		}
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
			double t1_limit = (minv + maxVelocity) * 200.0;
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
			maxNextV = minv + (60.0 - t1 - t2) * maxAcceleration;
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
			double t1_limit = (maxVelocity - maxv) * 200.0;
			t1 = Math.min(t1, t1_limit);
			double core = r2 - ((t1 - 60) * (t1 - 60));
			if (core < 0) {
				return null;
			}
			double t2 = 60 - Math.sqrt(core);
			minNextV = maxv + (t1 + t2 - 60.0) * maxAcceleration;
			if (DEBUG) {
				System.out.println(" => #(t1=" + t1 + ", t2=" + t2 + ", r^2="
						+ r2 + ")");
			}
		}
		// System.out.println("  => ###(" + minNextV + "," + maxNextV + ")");
		return new Range(Math.max(-maxVelocity, minNextV), Math.min(
				maxVelocity, maxNextV));
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

		public double getMinimumAbsoluteValue() {
			if (min > 0.0) {
				return min * 0.7 + max * 0.3;
			} else if (max < 0.0) {
				return max * 0.7 + min * 0.3;
			} else {
				return 0.0;
			}
		}

		public String toString() {
			return "(" + min + "," + max + ")";
		}
	}

}
