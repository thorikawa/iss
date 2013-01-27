package iss;
public class SingleAction {
	private double rotationDiff;

	private double velocityDiff;

	public SingleAction(double rotationDiff, double velocityDiff) {
		this.rotationDiff = rotationDiff;
		this.velocityDiff = velocityDiff;
	}

	public SingleState apply(SingleState s) {
		return new SingleState(s.getRotation() + rotationDiff, s.getVelocity()
				+ velocityDiff);
	}
}

