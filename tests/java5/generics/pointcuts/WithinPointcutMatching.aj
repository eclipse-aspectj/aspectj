public aspect WithinPointcutMatching {
	
	// rule 1) you can't use generic or parameterized type patterns with within
	pointcut tryGeneric() : within(Generic<T>); // CE L 4
	pointcut tryParameterized() : within(ISore<String>); // CE L5
	pointcut tryBeingSneaky() : within(String || (Number || Generic<Double>)); // CE L6
		
}


class Generic<T> {
	
	T foo = null;
	
	T getFoo() {
		return foo;
	}
	
}

interface ISore<E> {
	
	void iSee(E anE);
	
}

class UglyBuilding implements ISore<String> {
	
	public void iSee(String s) {}
	
}