package main.p2;

public abstract aspect AbstractTest {

	private int field;

	protected abstract pointcut pc();

	Object around(): pc() {
		this.field++;
		hook();
		return proceed();
	}

	protected final int getField() {
		return this.field;
	}

	protected abstract void hook();
}