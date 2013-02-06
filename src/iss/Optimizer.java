package iss;

import iss1.LibraryWrapper;

public class Optimizer {
	private static final int TYPE_SARJ = 0;

	private static final int TYPE_BGA = 0;

	public static final double SARJ_MAX_VEL = 0.15;

	public static final double SARJ_MAX_ACC = 0.005;

	public static final double BGA_MAX_VEL = 0.25;

	public static final double BGA_MAX_ACC = 0.01;

	private static final int MAX_ITERATION = 50000;

	private static final double DELTA = 0.1;

	private static final boolean DEBUG = false;

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

		// convert dimension
		int time = rotations.length;
		double[][] trRotations = new double[10][time];
		for (int i = 0; i < time; i++) {
			for (int j = 0; j < 10; j++) {
				trRotations[j][i] = rotations[i][j];
			}
		}
		// smooth BGA rotations considering 80 degree constraint
		for (int i = 2; i < 10; i++) {
			smooth(trRotations[i], 79.8);
		}

		// never use rotations below

		double optimizedVelocities[][] = new double[10][time];
		for (int i = 0; i < 10; i++) {
			int type = TYPE_BGA;
			if (i < 2) {
				type = TYPE_SARJ;
			}
			double[] tmp = this.optimize(trRotations[i], type);
			if (tmp == null) {
				System.out
						.println("[Error] Velocity could not be optimized for gear#:"
								+ i);
				return null;
			}
			for (int j = 0; j < time; j++) {
				optimizedVelocities[i][j] = tmp[j];
			}
		}

		/*
		 * if (DEBUG) { // evaluate for (int minute = 0; minute < time;
		 * minute++) { double[] newInput = new double[10]; newInput[0] =
		 * sarj1Rotations[minute]; newInput[1] = sarj2Rotations[minute]; for
		 * (int i = 2; i < 10; i++) { newInput[i] = rotations[minute][i]; }
		 * double score = libraryWrapper1.evaluate(newInput, minute, beta, yaw);
		 * System.out.println("score" + minute + ":" + score); } }
		 */

		double[][] res = new double[time][20];
		for (int i = 0; i < time; i++) {
			for (int j = 0; j < 10; j++) {
				res[i][j * 2] = trRotations[j][i];
				res[i][j * 2 + 1] = optimizedVelocities[j][i];
			}
		}
		return res;
	}

	/**
	 * optimize give rotation array and return velocity array
	 * 
	 * @param rotations
	 * @return
	 */
	public double[] optimize(double[] rotations, int type) {
		double[] velocities = null;
		for (int i = 0; i < MAX_ITERATION; i++) {

			if (DEBUG) {
				for (double r : rotations) {
					System.out.print(r);
					System.out.print(",");
				}
				System.out.println();
			}

			VelocityChecker velocityChecker = null;
			if (type == TYPE_SARJ) {
				velocityChecker = new VelocityChecker(SARJ_MAX_VEL,
						SARJ_MAX_ACC);
			} else if (type == TYPE_BGA) {
				velocityChecker = new VelocityChecker(BGA_MAX_VEL, BGA_MAX_ACC);
			} else {
				System.err.println("[Error] Unknown Type");
				return null;
			}
			velocities = velocityChecker.findPossibleVelocities(rotations);
			if (velocities != null) {
				if (DEBUG) {
					System.out.println("VelocityOptimizeDone.IterationCount:"
							+ i);
				}
				return velocities;
			}
			// improve rotations
			improve(rotations);
		}
		return null;
	}

	public static void improve(double[] rotations) {
		final int lastIndex = rotations.length - 1;

		// find maximum shift
		double maxAbsShift = -1.0;
		double maxShift = 0.0;
		int maxShiftIndex = -1;
		for (int j = 0; j < lastIndex; j++) {
			double shift = ISSUtils.determineShift(rotations[j],
					rotations[j + 1]);
			double absShift = Math.abs(shift);
			if (absShift > maxAbsShift) {
				maxAbsShift = absShift;
				maxShift = shift;
				maxShiftIndex = j;
			}
		}
		{
			// additional cyclic check
			double shift = ISSUtils.determineShift(rotations[lastIndex],
					rotations[0]);
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
				double shift = ISSUtils.determineShift(rotations[j],
						rotations[j + 1]);
				if (shift < minShift) {
					minShift = shift;
					otherIndex = j;
				}
			}
			double shift = ISSUtils.determineShift(rotations[lastIndex],
					rotations[0]);
			if (shift < minShift) {
				minShift = shift;
				otherIndex = lastIndex;
			}
			assert (maxShiftIndex != otherIndex);
			if (DEBUG) {
				System.out.println(maxShiftIndex + "+, " + otherIndex + "-");
			}
			rotations[maxShiftIndex] = ISSUtils
					.normalizeDegree(rotations[maxShiftIndex] + DELTA);
			rotations[otherIndex] = ISSUtils
					.normalizeDegree(rotations[otherIndex] - DELTA);
		} else {
			// 負の角度にMAX=最大値を探す
			int otherIndex = -1;
			double otherMaxShift = -1000.0;
			for (int j = 0; j < lastIndex; j++) {
				double shift = ISSUtils.determineShift(rotations[j],
						rotations[j + 1]);
				if (shift > otherMaxShift) {
					otherMaxShift = shift;
					otherIndex = j;
				}
			}
			double shift = ISSUtils.determineShift(rotations[lastIndex],
					rotations[0]);
			if (shift > otherMaxShift) {
				otherMaxShift = shift;
				otherIndex = lastIndex;
			}
			assert (maxShiftIndex != otherIndex);
			if (DEBUG) {
				System.out.println(maxShiftIndex + "-, " + otherIndex + "+");
			}
			rotations[maxShiftIndex] = ISSUtils
					.normalizeDegree(rotations[maxShiftIndex] - DELTA);
			rotations[otherIndex] = ISSUtils
					.normalizeDegree(rotations[otherIndex] + DELTA);
		}
	}

	public double[] smooth(double[] rotations, double maxSumDelta) {
		for (int i = 0; i < MAX_ITERATION; i++) {
			double delta = sumDelta(rotations);
			if (delta < maxSumDelta) {
				return rotations;
			}
			improve(rotations);
		}
		return rotations;
	}

	public static double sumDelta(double[] rotations) {
		double delta = 0.0;
		for (int i = 0; i < rotations.length - 1; i++) {
			delta += Math.abs(ISSUtils.determineShift(rotations[i],
					rotations[i + 1]));
		}
		delta += Math.abs(ISSUtils.determineShift(
				rotations[rotations.length - 1], rotations[0]));
		return delta;
	}

	public static double mean(double[] m) {
		double sum = 0;
		for (int i = 0; i < m.length; i++) {
			sum += (double) m[i] / (double) m.length;
		}
		return sum;
	}
}
