import java.util.*;

public aspect CallPointcutMatchingParamAndReturnTypes {
	
	void foo() {
		// make some calls!
		Generic<Number> gn = new Generic(new ArrayList<String>());
		new UglyBuilding().foo();
		gn = new Generic<Number>(5.0d);
		gn.foo();
		new UglyBuilding().iSee("you");
		new UglyBuilding().ic2it();
//		new UglyBuilding().iSee(new Object());
	}
	
	// rule 3) a raw parameter pattern matches any parameterized type
	declare warning : call(Generic.new(List)) 
	                  : "raw param type matching in call ok";
	declare warning : call(List UglyBuilding.foo())
	                  : "raw return type matching in call ok";
	
	// rule 4) A param type declared using a type variable is matched by its erasure
	declare warning : call(Generic.new(Object)) 
	  : "erasure type matching in call ok";
	declare warning : call(Object Generic.foo())
	 : "erasure type matching in call ok";
	
//	 rule 5) no join points in bridge methods  - test this separately for call...
	declare warning : call(void UglyBuilding.iSee(String))
	                  : "call and parameterized method ok";
	declare warning : call(* ISore.*(..))
	                  : "call and generic interface ok";
	declare warning : call(* I2.*(..))
	                  : "call and interface control test";
//	declare warning : call(void UglyBuilding.iSee(Object))
//	                  : "should be no join points for bridge methods";
	
	// rule 6) parameterized types in return and args can be matched exactly
	declare warning : call(Generic.new(List<String>)) : "match on parameterized args";
	declare warning : call(List<Number> *(..)) : "match on parameterized return type";
	
}


class Generic<T> {
	int x;
	public Generic(List<String> ls) {
		x = 5;
	}
	public Generic(T t) {
		x = 6;
	}
	
	T foo() { x = 7; return null; }
}

interface ISore<E> {
	
	void iSee(E anE);
	
}

interface I2 {
	void ic2it();
}

class UglyBuilding implements ISore<String>, I2 {
	
	int y;
	
	// this class will have a bridge method with signature void iSee(Object), with a cast and call
	// to the method below
	public void iSee(String s) {
		y = 2;
	}
	
	public void ic2it() { y = 4; }
	
	List<Number> foo() { y = 1; return null; }
}