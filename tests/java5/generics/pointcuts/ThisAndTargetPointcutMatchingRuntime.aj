public aspect ThisAndTargetPointcutMatchingRuntime {

	// rule 2) a raw type pattern matches all jps within a generic type
	pointcut setAndThis() : set(* *) && this(Generic);
	pointcut setAndTarget() : set(* *) && target(Generic);
	pointcut executionAndThis() : execution(* *(..)) && this(Generic);
	pointcut executionAndTarget() : execution(* *(..)) && target(Generic);
	pointcut callAndTarget() : call(* *(..)) && target(Generic);
	
	// rule 3) a raw type pattern matches all jps within a parameterized type
	pointcut pCallAndThis() : call(* *(..)) && this(ISore);
	pointcut pCallAndTarget() : call(* *(..)) && target(ISore);
	
	before() : setAndThis() { System.out.println("set and this matched ok"); }
	before() : setAndTarget() { System.out.println("set and target matched ok"); }
	before() : executionAndThis() { System.out.println("execution and this matched ok"); }
	before() : executionAndTarget() { System.out.println("execution and target matched ok"); }
	before() : callAndTarget() { System.out.println("call and target matched ok"); }
	before() : pCallAndThis() { System.out.println("parameterized call and this matched ok"); }
	before() : pCallAndTarget() { System.out.println("parameterized call and target matched ok"); }
	
	public static void main(String[] args) {
		Generic<String> gs = new Generic<String>();
		gs.getFoo();
		UglyBuilding theIBMBuilding = new UglyBuilding(); 
		theIBMBuilding.iSee("you");
	}
}


class Generic<T> {
	
	T foo = null;
	
	public T getFoo() {
		return foo;
	}
	
}

interface ISore<E> {
	
	void iSee(E anE);
	
}

class UglyBuilding implements ISore<String> {
	
	public void iSee(String s) {
		noop();
	}
	
	private void noop() {}
	
}