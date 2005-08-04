public aspect ExecutionPointcutMatchingErrorCases {
	
	// rule 1) you can't use generic or parameterized type patterns in the declaring type position
	pointcut tryExecutionGeneric() : execution(* Generic<T>.*(..)); // CE L 4
	pointcut tryExecutionParameterized() : execution(* Generic<String>.*(..)); // CE L5
	pointcut badThrows() : execution(* Generic.*(..) throws Ex*<String>); // CE L6
}


class Generic<T> {
	
	T foo = null;
	
	T getFoo() {
		return foo;
	}
	
}
