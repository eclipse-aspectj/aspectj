aspect LogManager {
	public void Loggable.logTrace(Object message) {
	}
	// no crash if the next method on the next line is renamed
	public void Loggable.logTrace(Object message, Throwable t)  {
	}
}

interface Loggable {
}
