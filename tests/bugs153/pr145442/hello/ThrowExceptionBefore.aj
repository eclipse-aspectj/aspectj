package hello;

public aspect ThrowExceptionBefore {

	before () : execution(public void println()) {
		throw new UnsupportedOperationException();
	}
}
