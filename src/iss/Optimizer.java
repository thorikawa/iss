package iss;

import iss1.LibraryWrapper;

public class Optimizer {
	private static final int MAX_ITERATION = 2000;

	private static final double DELTA = 0.1;

	private static final boolean DEBUG = true;

	private LibraryWrapper libraryWrapper1;

	public Optimizer() {
		libraryWrapper1 = new iss1.LibraryWrapper();
	}

	/**
	 * optimize answer which does not consider velocity and acceleration
	 * constraint.
	 */
	public double[][] optimize(double[][] rotations, double beta, double yaw) {

		libraryWrapper1.init(beta, yaw);

		double[] sarj1Rotations = new double[rotations.length];
		double[] sarj2Rotations = new double[rotations.length];
		for (int i = 0; i < rotations.length; i++) {
			sarj1Rotations[i] = rotations[i][0];
			sarj2Rotations[i] = rotations[i][1];
		}

		double[] sarj1Velocities = this.optimize(sarj1Rotations);
		double[] sarj2Velocities = this.optimize(sarj2Rotations);
		// double[] sarj2Velocities = null;

		if (sarj1Velocities == null || sarj2Velocities == null) {
			System.err.println("[Error] Velocity could not be optimized.");
			return null;
		}

		double[][] res = new double[rotations.length][20];
		for (int i = 0; i < rotations.length; i++) {
			for (int j = 0; j < 10; j++) {
				if (j == 0) {
					res[i][j * 2] = rotations[i][j];
					res[i][j * 2 + 1] = sarj1Velocities[i];
				} else if (j == 1) {
					res[i][j * 2] = rotations[i][j];
					res[i][j * 2 + 1] = sarj2Velocities[i];
				} else {
					res[i][j * 2] = rotations[i][j];
					res[i][j * 2 + 1] = 0.0;
				}
			}
		}
		return res;
	}

	/**
	 * optimize give rotation array and return velocity array
	 * 
	 * @param sarjRotations
	 * @return
	 */
	public double[] optimize(double[] sarjRotations) {
		double[] sarjVelocities = null;
		final int lastIndex = sarjRotations.length - 1;
		for (int i = 0; i < MAX_ITERATION; i++) {
			if (i == MAX_ITERATION - 1) {
				VelocityChecker.DEBUG = true;
			}
			if (DEBUG) {
				for (double r : sarjRotations) {
					System.out.print(r);
					System.out.print(",");
				}
				System.out.println();
			}
			sarjVelocities = VelocityChecker
					.findPossibleVelocities(sarjRotations);
			if (sarjVelocities != null) {
				return sarjVelocities;
			}
			// improve rotations
			// find maximum shift
			double maxAbsShift = -1.0;
			double maxShift = 0.0;
			int maxShiftIndex = -1;
			for (int j = 0; j < lastIndex; j++) {
				double shift = ISSUtils.determineShift(sarjRotations[j],
						sarjRotations[j + 1]);
				double absShift = Math.abs(shift);
				if (absShift > maxAbsShift) {
					maxAbsShift = absShift;
					maxShift = shift;
					maxShiftIndex = j;
				}
			}
			{
				// additional cyclic check
				double shift = ISSUtils.determineShift(
						sarjRotations[lastIndex], sarjRotations[0]);
				double absShift = Math.abs(shift);
				if (absShift > maxAbsShift) {
					maxAbsShift = absShift;
					maxShift = shift;
					maxShiftIndex = lastIndex;
				}
			}

			if (maxShift > 0.0) {
				// 正の角度にMAX=最小値を探す
				int otherIndex = -1;
				double minShift = 1000.0;
				for (int j = 0; j < lastIndex; j++) {
					double shift = ISSUtils.determineShift(sarjRotations[j],
							sarjRotations[j + 1]);
					if (shift < minShift) {
						minShift = shift;
						otherIndex = j;
					}
				}
				double shift = ISSUtils.determineShift(
						sarjRotations[lastIndex], sarjRotations[0]);
				if (shift < minShift) {
					minShift = shift;
					otherIndex = lastIndex;
				}
				assert (maxShiftIndex != otherIndex);
				sarjRotations[maxShiftIndex] = ISSUtils
						.normalizeDegree(sarjRotations[maxShiftIndex] + DELTA);
				sarjRotations[otherIndex] = ISSUtils
						.normalizeDegree(sarjRotations[otherIndex] - DELTA);
			} else {
				// 負の角度にMAX=最大値を探す
				int otherIndex = -1;
				double otherMaxShift = -1000.0;
				for (int j = 0; j < lastIndex; j++) {
					double shift = ISSUtils.determineShift(sarjRotations[j],
							sarjRotations[j + 1]);
					if (shift > otherMaxShift) {
						otherMaxShift = shift;
						otherIndex = j;
					}
				}
				double shift = ISSUtils.determineShift(
						sarjRotations[lastIndex], sarjRotations[0]);
				if (shift > otherMaxShift) {
					otherMaxShift = shift;
					otherIndex = lastIndex;
				}
				assert (maxShiftIndex != otherIndex);
				sarjRotations[maxShiftIndex] = ISSUtils
						.normalizeDegree(sarjRotations[maxShiftIndex] - DELTA);
				sarjRotations[otherIndex] = ISSUtils
						.normalizeDegree(sarjRotations[otherIndex] + DELTA);
			}
		}
		return null;
	}
}
