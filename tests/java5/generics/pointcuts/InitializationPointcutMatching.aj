public aspect InitializationPointcutMatching {
	
	// rule 1) you can't use generic or parameterized type patterns in the declaring type position
	pointcut tryInitGeneric() : initialization(Generic<T>.new(..)); // CE L 4
	pointcut tryPreInitGeneric() : preinitialization(Generic<T>.new(..)); // CE L5
	pointcut tryInitParameterized() : initialization(Generic<String>.new(..)); // CE L6
	pointcut tryPreInitParameterized() : preinitialization(Generic<String>.new(..)); // CE L7
	pointcut trySneakyInit() : initialization((Object || Generic<Number>).new(..)); // CE L8
	pointcut badThrows() : initialization(Generic.new(..) throws Ex*<String>); // CE L9
}


class Generic<T> {
	
	T foo = null;
	
	T getFoo() {
		return foo;
	}
	
}
