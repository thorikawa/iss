package iss;

public class OptimizerTest {
	public static void main(String[] args) {
		double[][] rotations = RawResult.DATA_P75;
		double beta = -70;
		double input[] = new double[rotations.length];
		for (int i = 0; i < rotations.length; i++) {
			input[i] = rotations[i][0];
		}
		Optimizer optimizer = new Optimizer();
		double[][] answer = optimizer.optimize(rotations, beta, 0);
		if (answer != null) {
			for (double[] a : answer) {
				System.out.print("{");
				for (double b : a) {
					System.out.print(b + ",");
				}
				System.out.println("},");
			}
		}

		/*
		 * int target = 3; double input2[] = new double[rotations.length]; for
		 * (int i = 0; i < rotations.length - 1; i++) { input2[i] =
		 * rotations[i][target]; System.out.println(input2[i]); }
		 * System.out.println("==========="); optimizer.smooth(input2, 80.0);
		 * for (double a : input2) { System.out.println(a); }
		 */
	}
}
