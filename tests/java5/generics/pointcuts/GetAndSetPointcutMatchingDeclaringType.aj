public aspect GetAndSetPointcutMatchingDeclaringType {
	
	// rule 2) a raw declaring type pattern matches any parameterized or generic type
	
	declare warning : get(* Generic.*) : "generic/param get matching ok";  // CW L15,33
	declare warning : set(* Generic.*) : "generic/param set matching ok";  // CW L12,32
}


class Generic<T> {
	
	public T foo = null;
	
	T getFoo() {
		return foo;
	}
	
}

interface ISore<E> {
	
	public static final int x = 5;
	
	void iSee(E anE);
	
}

class UglyBuilding implements ISore<String> {
	
	public void iSee(String s) {
		Generic<String> gs = new Generic<String>();
		gs.foo = "hi";  // set jp
		s.equals(gs.foo); // get jp
	}
	
}