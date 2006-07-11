package hello;

public aspect ThrowExceptionAfter {

	after () : execution(public void println()) {
		throw new UnsupportedOperationException();
	}
}
