package hello;

public aspect ThrowExceptionAround {

	void around () : execution(public void println()) {
		throw new UnsupportedOperationException();
	}
}
