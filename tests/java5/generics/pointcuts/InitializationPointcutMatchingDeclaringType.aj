public aspect InitializationPointcutMatchingDeclaringType {
	
	// rule 2) a raw declaring type pattern matches any generic type
	
	declare warning : initialization(Generic.new(..)) : "generic/param init matching ok";  // CW L15,33
	declare warning : preinitialization(Generic.new(..)) : "generic/param preinit matching ok";  // CW L12,32
}


class Generic<T> {
	
	public T foo = null;
	
	T getFoo() {
		return foo;
	}
	
}
