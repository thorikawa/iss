package iss;
import java.text.DecimalFormat;

public class SingleState {

	public static SingleState ZERO = new SingleState(0.0, 0.0);

	public static SingleState ROT_180 = new SingleState(180.0, 0.0);

	public static SingleState ROT_MINUS_180 = new SingleState(-180.0, 0.0);

	private double rotation;

	private double velocity;

	DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

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

	protected SingleState copy() {
		return new SingleState(this.getRotation(), this.getVelocity());
	}

	public void addRotation(double diff) {
		this.rotation = ISSUtils.normalizeDegree(rotation + diff);
	}

	@Override
	public String toString() {
		return DECIMAL_FORMAT.format(rotation);
	}
}
