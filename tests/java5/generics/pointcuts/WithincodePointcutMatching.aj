public aspect WithincodePointcutMatching {
	
	// rule 1) you can't use generic or parameterized type patterns in the declaring type position
	pointcut tryWcGeneric() : withincode(* Generic<T>.*(..)); // CE L 4
	pointcut tryWcParameterized() : withincode(* Generic<String>.*(..)); // CE L5
	pointcut badThrows() : withincode(* Generic.*(..) throws Ex*<String>); // CE L6
}


class Generic<T> {
	
	T foo = null;
	
	T getFoo() {
		return foo;
	}
	
}
