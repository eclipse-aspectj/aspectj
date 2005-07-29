public aspect WithinPointcutMatchingWarnings {
	
	// rule 2) a raw type pattern matches all jps within a generic type
	declare warning : set(* *) && within(Generic) : "matched set correctly";
	declare warning : execution(* *(..)) && within(Generic) : "matched execution correctly";
	
	// rule 3) a raw type pattern with + matches all jps within a parameterized type
	declare warning : within(ISore) : "init matched correctly";
	declare warning : execution(* *(..)) && within(ISore+) : "matched parameterization ok";
		
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