package main.p2;

public abstract aspect AbstractTest {

	private int field;
	protected String s = "test";

	protected abstract pointcut pc();

	Object around(): pc() {
		this.field++;
		s += "-1";
		hook();
		return proceed();
	}

	protected final int getField() {
		return this.field;
	}

	protected abstract void hook();
}