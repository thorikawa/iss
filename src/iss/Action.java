package iss;
public class Action {

	private SingleAction singleActions[] = new SingleAction[10];

	public void setSingleAction(int i, SingleAction s) {
		singleActions[i] = s;
	}

	public State apply(State currentState) {
		State newState = new State();
		SingleState[] singleStates = currentState.getSingleStates();
		for (int i = 0; i < 10; i++) {
			newState.setSingleState(i, singleActions[i].apply(singleStates[i]));
		}
		return newState;
	}
}