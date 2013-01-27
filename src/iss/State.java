package iss;
public class State {

	private SingleState singleStates[] = new SingleState[10];

	public void setSingleState(int i, SingleState s) {
		singleStates[i] = s.copy();
	}

	public double[] getRotations() {
		double rotations[] = new double[10];
		for (int i = 0; i < 10; i++) {
			rotations[i] = singleStates[i].getRotation();
		}
		return rotations;
	}

	public SingleState[] getSingleStates() {
		return singleStates;
	}

	public SingleState getSingleState(int i) {
		return singleStates[i];
	}

	public State copy() {
		State newState = new State();
		for (int i = 0; i < singleStates.length; i++) {
			newState.setSingleState(i, singleStates[i].copy());
		}
		return newState;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < singleStates.length; i++) {
			sb.append(singleStates[i].toString());
			if (i != singleStates.length - 1)
				sb.append(",");
		}
		return sb.toString();
	}
}
