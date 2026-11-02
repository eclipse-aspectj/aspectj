public sealed abstract class Shape permits Circle, Square {
	public String describe() { return getClass().getName(); }
}
