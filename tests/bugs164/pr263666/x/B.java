package x;

public class B {
	private int b() {
		return b();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		b();
		return super.clone();
	}
}
