public aspect ThisAndTargetPointcutMatching {
	
	// rule 1) you can't use generic or parameterized type patterns with this and target
	pointcut tryThisGeneric() : this(Generic<T>); // CE L 4
	pointcut tryTargetGeneric() : target(Generic<T>); // CE L5
	pointcut tryThisParameterized() : this(ISore<String>); // CE L6
	pointcut tryTargetParameterized() : target(ISore<String>); // CE /7	
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