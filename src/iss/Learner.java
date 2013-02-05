package iss;

import iss1.LibraryWrapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Learner {
	private double beta;

	private double yaw;

	private static iss1.LibraryWrapper libraryWrapper1 = new iss1.LibraryWrapper();

	private static iss2.LibraryWrapper libraryWrapper2 = new iss2.LibraryWrapper();

	private static String outFileName = "out2.txt";

	private OutputStreamWriter osw;

	/**
	 * basic steepest descent method.
	 */
	private static final int DESCENT_TYPE_NORMAL = 1;

	/**
	 * improved steepest descent method. to avoid local optima, use random start
	 * points.
	 */
	private static final int DESCENT_TYPE_RANDOM = 2;

	private static int DESCENT_TYPE = DESCENT_TYPE_NORMAL;

	public static void main(String[] args) {

		double beta = 0;
		double yaw = 0;
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
		if (args.length == 3) {
			outFileName = args[1];
			DESCENT_TYPE = Integer.parseInt(args[2]);
		}

		libraryWrapper1.init(beta, yaw);
		libraryWrapper2.init(beta, yaw);

		Learner instance = new Learner(beta, yaw);
		State initialState = new State();
		if (beta > 0) {
			initialState.setSingleState(0, SingleState.ZERO);
			initialState.setSingleState(1, SingleState.ZERO);
			initialState.setSingleState(2, SingleState.ZERO);
			initialState.setSingleState(3, SingleState.ROT_180);
			initialState.setSingleState(4, SingleState.ZERO);
			initialState.setSingleState(5, SingleState.ROT_180);
			initialState.setSingleState(6, SingleState.ZERO);
			initialState.setSingleState(7, SingleState.ROT_180);
			initialState.setSingleState(8, SingleState.ZERO);
			initialState.setSingleState(9, SingleState.ROT_180);
		} else {
			initialState.setSingleState(0, SingleState.ZERO);
			initialState.setSingleState(1, SingleState.ZERO);
			initialState.setSingleState(2, SingleState.ROT_180);
			initialState.setSingleState(3, SingleState.ZERO);
			initialState.setSingleState(4, SingleState.ROT_180);
			initialState.setSingleState(5, SingleState.ZERO);
			initialState.setSingleState(6, SingleState.ROT_180);
			initialState.setSingleState(7, SingleState.ZERO);
			initialState.setSingleState(8, SingleState.ROT_180);
			initialState.setSingleState(9, SingleState.ZERO);
		}
		instance.learn(initialState);
	}

	public Learner(double beta, double yaw) {
		this.beta = beta;
		this.yaw = yaw;
		try {
			osw = new OutputStreamWriter(new FileOutputStream(outFileName));
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public void learn(State initialState) {

		try {
			osw.write("beta=");
			osw.write(String.valueOf(this.beta));
			osw.write("\n");
			osw.write("yaw=");
			osw.write(String.valueOf(this.yaw));
			osw.write("\n");
			osw.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// this.initialState = initialState;
		State state = initialState;
		try {
			osw.write("{\n");
			for (int i = 0; i < 92; i++) {
				if (DESCENT_TYPE == DESCENT_TYPE_RANDOM) {
					state = this.randomGradientDescent(state, i);
				} else {
					state = this.gradientDescent(state, i);
				}
				osw.write("{ ");
				osw.write(state.toString());
				osw.write("}");
				if (i != 91) {
					osw.write(",");
				}
				osw.write("\n");
				osw.flush();
				LibraryWrapper.proceed(state);
			}
			osw.write("\n}");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static final double DIFF = 0.1;

	private static final double ALPHA = 0.001;

	private static final double THRESHOLD = 0.1;

	private static final int ENTIRE_LOOP_COUNT = 10;

	private static final int MAX_SINGLE_LOOP_COUNT = 100;

	private State gradientDescent(final State startState, final int minute) {
		State startStateCopy = startState.copy();
		for (int k = 0; k < ENTIRE_LOOP_COUNT; k++) {
			System.out.println("loop1:" + k);
			for (int i = 0; i < 10; i++) {
				System.out.println(" loop2 roration:" + i);
				SingleState startTargetState = startStateCopy.getSingleState(i);
				for (int loop = 0; loop < MAX_SINGLE_LOOP_COUNT; loop++) {
					// System.err.println("  loop3:" + loop);
					// System.err.println(startState);
					final State state1 = startState.copy();
					final State state2 = startState.copy();
					state1.getSingleState(i).addRotation(DIFF);
					state2.getSingleState(i).addRotation(-DIFF);
					FutureTask<Double> task1 = new FutureTask<Double>(
							new Callable<Double>() {
								@Override
								public Double call() throws Exception {
									double score1 = libraryWrapper1.evaluate(
											state1, minute, Learner.this.beta,
											Learner.this.yaw);
									return score1;
								}
							});
					FutureTask<Double> task2 = new FutureTask<Double>(
							new Callable<Double>() {
								@Override
								public Double call() throws Exception {
									double score2 = libraryWrapper2.evaluate(
											state2, minute, Learner.this.beta,
											Learner.this.yaw);
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
					double dDegree = ALPHA * d;
					SingleState targetState = startState.getSingleState(i);
					targetState.addRotation(dDegree);
				}
			}
		}
		return startState;
	}

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
		for (int loop = 0; loop < MAX_SINGLE_LOOP_COUNT / 2; loop++) {
			final State state1 = startState.copy();
			final State state2 = startState.copy();
			state1.getSingleState(targetIndex).addRotation(DIFF);
			state2.getSingleState(targetIndex).addRotation(-DIFF);
			FutureTask<Double> task1 = new FutureTask<Double>(
					new Callable<Double>() {
						@Override
						public Double call() throws Exception {
							double score1 = libraryWrapper1
									.evaluate(state1, minute,
											Learner.this.beta, Learner.this.yaw);
							return score1;
						}
					});
			FutureTask<Double> task2 = new FutureTask<Double>(
					new Callable<Double>() {
						@Override
						public Double call() throws Exception {
							double score2 = libraryWrapper2
									.evaluate(state2, minute,
											Learner.this.beta, Learner.this.yaw);
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
					if (shift > 15.0 || shift < -15.0) {
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
			for (int i = 0; i < 10; i++) {
				System.out.println(" loop2 roration:" + i);

				// Consider how far from the initial point the learned rotation
				// is.
				double baseRotation = startStateCopy.getSingleState(i)
						.getRotation();

				double score0 = libraryWrapper1.evaluate(startState, minute,
						Learner.this.beta, Learner.this.yaw);

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
							Learner.this.beta, Learner.this.yaw);
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

class ActionIterable implements Iterable<Action> {

	@Override
	public Iterator<Action> iterator() {
		return new ActionIterator();
	}

}

class ActionIterator implements Iterator<Action> {

	private static final SingleAction bgaActions[] = {
			new SingleAction(8.7, 0), new SingleAction(-8.7, 0),
			new SingleAction(0, 0) };

	private static final SingleAction sarjActions[] = {
			new SingleAction(4.5, 0), new SingleAction(-4.5, 0),
			new SingleAction(0, 0) };

	private static final int base = bgaActions.length;

	private static final int countMax = (int) Math.pow(base, 6);

	private static Map<Integer, Integer> actionMapping = new HashMap<Integer, Integer>();
	static {
		actionMapping.put(0, 0);
		actionMapping.put(1, 1);
		actionMapping.put(2, 2);
		actionMapping.put(3, 3);
		actionMapping.put(4, 2);
		actionMapping.put(5, 3);
		actionMapping.put(6, 4);
		actionMapping.put(7, 5);
		actionMapping.put(8, 4);
		actionMapping.put(9, 5);
	}

	private int i = 0;

	@Override
	public boolean hasNext() {
		if (i < countMax)
			return true;
		else
			return false;
	}

	@Override
	public Action next() {
		int j = i;
		SingleAction singleActions[] = new SingleAction[6];
		for (int rank = 0; rank < 6; rank++) {
			int index = j % base;
			j /= base;
			if (rank < 2) {
				singleActions[rank] = sarjActions[index];
			} else {
				singleActions[rank] = bgaActions[index];
			}
		}

		Action action = new Action();
		for (int k = 0; k < 10; k++) {
			action.setSingleAction(k, singleActions[actionMapping.get(k)]);
		}
		i++;
		return action;
	}

	@Override
	public void remove() {
		// DO NOTHING
	}

}
