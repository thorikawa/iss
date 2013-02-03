package iss1;

import iss.SingleState;
import iss.State;

public class LibraryWrapper {

	private ConstraintsChecker checker;

	public LibraryWrapper() {
		ISSVis.main(new String[0]);
		checker = new ConstraintsChecker();
	}

	public void init(double beta, double yaw) {
		checker.setYaw(yaw);
		checker.setBeta(beta);
		System.out.println(yaw);
		System.out.flush();
	}

	public double evaluate(State state, int minute, double beta, double yaw) {
		double[] input = getInputParameter(state.getRotations(), minute, beta,
				yaw);
		double[] out = checker.evaluateSingleState(input);
		return calcScore(out);
	}

	public double evaluate(double[] rotations, int minute, double beta,
			double yaw) {
		double[] input = getInputParameter(rotations, minute, beta, yaw);
		double[] out = checker.evaluateSingleState(input);
		return calcScore(out);
	}

	private static double calcScore(double[] out) {
		double totalPower = 0.0;
		for (int i = 0; i < 8; i++) {
			double power = 0.0;
			for (int j = 0; j < 82; j++) {
				int stringShadowIndex = i * 82 + j + 8;
				double shadowFactor = Math.max(0.0,
						1.0 - 5.0 * out[stringShadowIndex]);
				power += 1371.3 * out[i] * 0.1 * 2.56 * shadowFactor;
			}
			totalPower += power;
		}
		return totalPower;
	}

	public static void proceed(State state) {
		System.out.println(1);
		SingleState[] singleStates = state.getSingleStates();
		for (SingleState ss : singleStates) {
			System.out.println(ss.getRotation());
			System.out.println(ss.getVelocity());
		}
		System.out.flush();

	}

	private static double[] getInputParameter(double[] rotations, int minute,
			double beta, double yaw) {
		double alpha = 360.0 * minute / 92.0;
		double[] input = new double[14];
		input[1] = alpha;
		input[2] = beta;
		input[3] = yaw;
		int i = 4;
		for (double rotation : rotations) {
			input[i++] = rotation;
		}
		return input;
	}
}
