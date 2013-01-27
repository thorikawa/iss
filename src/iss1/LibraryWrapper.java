package iss1;

import iss.SingleState;
import iss.State;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LibraryWrapper {

	private BufferedReader reader;

	private ConstraintsChecker checker;

	public LibraryWrapper() {
		InputStreamReader isr = new InputStreamReader(System.in);
		reader = new BufferedReader(isr);

		ISSVis.main(new String[0]);
		checker = new ConstraintsChecker();
	}

	public void init(double beta, double yaw) {
		checker.setYaw(yaw);
		checker.setBeta(beta);
		System.out.println(yaw);
		System.out.flush();
	}

	private static final int RENDER_LIBRARY = 0;

	public double evaluate(State state, int minute, double beta, double yaw) {
		double[] out = this.library(state, minute, beta, yaw);
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

	public double[] library(State state, int minute, double beta, double yaw) {
		double[] input = getInputParameter(state, minute, beta, yaw);
		double[] ret = checker.evaluateSingleState(input);
		return ret;
	}

	private static double[] getInputParameter(State state, int minute,
			double beta, double yaw) {
		double alpha = 360.0 * minute / 92.0;
		double[] rotations = state.getRotations();

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
