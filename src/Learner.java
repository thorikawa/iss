import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Learner {
    double beta;

    double yaw;

    private State initialState;

    static LibraryWrapper libraryWrapper = new LibraryWrapper();

    public static void main(String[] args) {

        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(isr);
        double beta = 0;
        try {
            beta = Double.parseDouble(reader.readLine());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        libraryWrapper.init(beta, 0.0);

        Learner instance = new Learner(beta, 0);
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
    }

    public void learn(State initialState) {
        this.initialState = initialState;
        State state = initialState;
        for (int i = 0; i < 92; i++) {
            if (i != 0) {
                state = this.getNextMaxState(state, i - 1);
            }
            LibraryWrapper.proceed(state);
        }
    }

    private State getNextMaxState(State currentState, int currentMinute) {
        State maxState = null;
        double maxScore = 0;
        ActionIterable actionIterable = new ActionIterable();
        for (Action action : actionIterable) {
            State nextState = action.apply(currentState);
            if (!this.canBeCyclic(nextState, currentMinute + 1)) {
                continue;
            }

            double score = libraryWrapper.evaluate(nextState, currentMinute + 1, this.beta, this.yaw);
            System.err.println(score);

            if (maxState == null || maxScore < score) {
                maxScore = score;
                maxState = nextState;
            }
        }
        return maxState;
    }

    private boolean canBeCyclic(State state, int minute) {
        double initials[] = this.initialState.getRotations();
        double rotations[] = state.getRotations();
        for (int i = 0; i < 2; i++) {
            if (ISSUtils.minDegree(rotations[i], initials[i]) > (92 - minute) * 4.5) {
                return false;
            }
        }
        for (int i = 2; i < 10; i++) {
            if (ISSUtils.minDegree(rotations[i], initials[i]) > (92 - minute) * 8.7) {
                return false;
            }
        }
        return true;
    }

}

class SingleState {

    public static SingleState ZERO = new SingleState(0.0, 0.0);

    public static SingleState ROT_180 = new SingleState(180.0, 0.0);

    public static SingleState ROT_MINUS_180 = new SingleState(-180.0, 0.0);

    private double rotation;

    private double velocity;

    public SingleState(double rotation, double velocity) {
        this.rotation = ISSUtils.normalizeDegree(rotation);
        this.velocity = velocity;
    }

    /**
     * @return degree from 0 (inclusive) to 360 (exclusive).
     */
    public double getRotation() {
        return rotation;
    }

    public double getVelocity() {
        return velocity;
    }

}

class SingleAction {
    private double rotationDiff;

    private double velocityDiff;

    public SingleAction(double rotationDiff, double velocityDiff) {
        this.rotationDiff = rotationDiff;
        this.velocityDiff = velocityDiff;
    }

    public SingleState apply(SingleState s) {
        return new SingleState(s.getRotation() + rotationDiff, s.getVelocity() + velocityDiff);
    }
}

class State {

    private SingleState singleStates[] = new SingleState[10];

    public void setSingleState(int i, SingleState s) {
        singleStates[i] = s;
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

}

class ActionIterable implements Iterable<Action> {

    @Override
    public Iterator<Action> iterator() {
        return new ActionIterator();
    }

}

class ActionIterator implements Iterator<Action> {

    private static final SingleAction bgaActions[] = {new SingleAction(8.7, 0), new SingleAction(-8.7, 0), new SingleAction(0, 0) };

    private static final SingleAction sarjActions[] = {new SingleAction(4.5, 0), new SingleAction(-4.5, 0), new SingleAction(0, 0) };

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

class Action {

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