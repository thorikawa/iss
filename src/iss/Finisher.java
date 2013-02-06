package iss;

import iss1.LibraryWrapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Finisher {
	private double beta;

	private double yaw;

	private static iss1.LibraryWrapper libraryWrapper1 = new iss1.LibraryWrapper();

	private static iss2.LibraryWrapper libraryWrapper2 = new iss2.LibraryWrapper();

	private static String outFileName = "out2.txt";

	private OutputStreamWriter osw;

	public static void main(String[] args) {

		double beta = 0;
		double yaw = 0;
		// FIXME
		double[][] data = CopyOfISS.getM71();

		if (args.length > 0) {
			beta = Double.parseDouble(args[0]);
		} else {
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(isr);
			try {
				beta = Double.parseDouble(reader.readLine());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (args.length == 2) {
			outFileName = args[1];
		}

		libraryWrapper1.init(beta, yaw);
		libraryWrapper2.init(beta, yaw);

		Finisher instance = new Finisher(beta, yaw);
		instance.learn();
	}

	public Finisher(double beta, double yaw) {
		this.beta = beta;
		this.yaw = yaw;
		try {
			osw = new OutputStreamWriter(new FileOutputStream(outFileName));
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00000000");

	public void learn() {

		// this.initialState = initialState;

		double data[][] = null;
		if (beta == -71) {
			data = CopyOfISS.getM71();
		} else if (beta == -73) {
			data = CopyOfISS.dataM73;
		} else if (beta == -75) {
			data = CopyOfISS.dataM75;
		} else if (beta == 73) {
			data = CopyOfISS.getP73();
		}

		double[][] optimizeInput = new double[data.length][10];

		try {
			osw.write("{\n");
			osw.flush();
			for (int i = 0; i < 92; i++) {
				State state = new State();
				for (int j = 0; j < 10; j++) {
					state.setSingleState(j, new SingleState(data[i][j * 2],
							data[i][j * 2 + 1]));
				}
				state = this.randomGradientDescent(state, i);
				osw.write("{ ");
				osw.write(state.toString());
				double[] rotations = state.getRotations();
				optimizeInput[i] = rotations;

				osw.write("}");
				if (i != 91) {
					osw.write(",");
				}
				osw.write("\n");
				osw.flush();
				LibraryWrapper.proceed(state);
			}
			osw.write("\n}");
			osw.flush();

			Optimizer optimizer = new Optimizer();
			double[][] answer = optimizer.optimize(optimizeInput, beta, 0);
			osw.write("============");
			osw.write("{\n");
			for (double[] oneline : answer) {
				osw.write("{ ");
				for (double d : oneline) {
					osw.write(DECIMAL_FORMAT.format(d));
					osw.write(", ");
				}
				osw.write("}, ");
				osw.write("\n");
			}
			osw.write("}\n");
			osw.flush();
			osw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// private static final double DIFF = 0.1;
	private static final double DIFF = 0.05;

	// private static final double ALPHA = 0.001;
	private static final double ALPHA = 0.0005;

	private static final double THRESHOLD = 0.1;

	private static final int ENTIRE_LOOP_COUNT = 5;

	private static final int MAX_SINGLE_LOOP_COUNT = 50;

	private static final int DIRECTION_PLUS = 1;

	private static final int DIRECTION_MINUS = 2;

	private static final int MAX_OPPOSITE_DIRECTION_COUNT = 10;

	/**
	 * ***Warning*** this method break startState!!
	 * 
	 * @param startState
	 * @param minute
	 * @param targetIndex
	 * @return
	 */
	private State gradientDescentSingle(final State startState,
			final int minute, int targetIndex, double baseRotation) {

		int prevDirection = -1;
		int oppositeDirectionCount = 0;

		// reduce the loop count, since we start with some start points.
		for (int loop = 0; loop < MAX_SINGLE_LOOP_COUNT; loop++) {
			final State state1 = startState.copy();
			final State state2 = startState.copy();
			state1.getSingleState(targetIndex).addRotation(DIFF);
			state2.getSingleState(targetIndex).addRotation(-DIFF);
			FutureTask<Double> task1 = new FutureTask<Double>(
					new Callable<Double>() {
						@Override
						public Double call() throws Exception {
							double score1 = libraryWrapper1.evaluate(state1,
									minute, Finisher.this.beta,
									Finisher.this.yaw);
							return score1;
						}
					});
			FutureTask<Double> task2 = new FutureTask<Double>(
					new Callable<Double>() {
						@Override
						public Double call() throws Exception {
							double score2 = libraryWrapper2.evaluate(state2,
									minute, Finisher.this.beta,
									Finisher.this.yaw);
							return score2;
						}
					});
			double score1 = 0;
			double score2 = 0;
			try {
				Thread thread1 = new Thread(task1);
				Thread thread2 = new Thread(task2);
				thread1.start();
				thread2.start();
				score1 = task1.get();
				score2 = task2.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.err.println(score1 + ":" + score2);
			double d = (score1 - score2) / (DIFF + DIFF);
			if (Math.abs(d) < THRESHOLD) {
				break;
			}
			if (d > 0) {
				if (prevDirection == DIRECTION_MINUS) {
					oppositeDirectionCount++;
				} else {
					oppositeDirectionCount = 0;
				}
				prevDirection = DIRECTION_PLUS;
			} else { // d < 0
				if (prevDirection == DIRECTION_PLUS) {
					oppositeDirectionCount++;
				} else {
					oppositeDirectionCount = 0;
				}
				prevDirection = DIRECTION_MINUS;
			}
			if (oppositeDirectionCount > MAX_OPPOSITE_DIRECTION_COUNT) {
				break;
			}
			double dDegree = ALPHA * d;
			SingleState targetState = startState.getSingleState(targetIndex);
			targetState.addRotation(dDegree);
			if (minute > 0) {
				// range check
				double shift = ISSUtils.determineShift(baseRotation,
						targetState.getRotation());
				if (targetIndex == 0) {
					// SARJ1
					if (shift < -9.0 || shift > 18.0) {
						// SARJ1 is usually increased.
						break;
					}
				} else if (targetIndex == 1) {
					// SARJ1
					if (shift > 9.0 || shift < -18.0) {
						// SARJ1 is usually decreased.
						break;
					}
				} else {
					// if (shift > 15.0 || shift < -15.0) {
					if (shift > 5.0 || shift < -5.0) {
						// BGA
						break;
					}
				}
			}
		}
		return startState;
	}

	private State randomGradientDescent(State startState, final int minute) {
		final State startStateCopy = startState.copy();

		// improve "startState"
		for (int k = 0; k < ENTIRE_LOOP_COUNT; k++) {
			System.out.println("loop1:" + k);
			// for (int i = 0; i < 10; i++) {
			// don't optimize sarj anymore
			for (int i = 2; i < 10; i++) {
				System.out.println(" loop2 roration:" + i);

				// Consider how far from the initial point the learned rotation
				// is.
				double baseRotation = startStateCopy.getSingleState(i)
						.getRotation();

				double score0 = libraryWrapper1.evaluate(startState, minute,
						Finisher.this.beta, Finisher.this.yaw);

				State maxState = startState;
				double maxScore = score0;

				int maxRandomLoop = 1;
				if (i < 2) {
					maxRandomLoop = 3;
				}

				State preResultState = startState;
				for (int randomLoop = 0; randomLoop < maxRandomLoop; randomLoop++) {

					State input = startState.copy();
					// leap 1.0*randomLoopCount
					double preRotation = preResultState.getSingleState(i)
							.getRotation();
					if (i == 0) {
						double preAdded = ISSUtils.determineShift(startState
								.getSingleState(i).getRotation(), preRotation);
						if (preAdded < 0) {
							input.getSingleState(i).addRotation(
									DIFF * 10 * randomLoop);
						} else {
							input.getSingleState(i).addRotation(
									preAdded + DIFF * 10 * randomLoop);
						}
					} else if (i == 1) {
						double preAdded = ISSUtils.determineShift(startState
								.getSingleState(i).getRotation(), preRotation);
						if (preAdded > 0) {
							input.getSingleState(i).addRotation(
									-DIFF * 10 * randomLoop);
						} else {
							input.getSingleState(i).addRotation(
									preAdded - DIFF * 10 * randomLoop);
						}
					}

					// System.out.println("try:input rotation="+
					// input.getSingleState(i).getRotation());
					gradientDescentSingle(input, minute, i, baseRotation); // input
																			// is
					// modified
					double score = libraryWrapper1.evaluate(input, minute,
							Finisher.this.beta, Finisher.this.yaw);
					System.out.println("output1: rotation="
							+ input.getSingleState(i).getRotation()
							+ ", score=" + score);
					if (score > maxScore) {
						maxState = input;
						maxScore = score;
					}
					preResultState = input;
				}

				startState = maxState;
			}
		}
		return startState;
	}
}
