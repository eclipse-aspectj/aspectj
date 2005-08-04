import java.util.*;

public aspect ExecutionPointcutMatchingParamAndReturnTypes {
	
	// rule 3) a raw parameter pattern matches any parameterized type
	declare warning : execution(Generic.new(List)) 
	                  : "raw param type matching in execution ok";
	declare warning : execution(List UglyBuilding.foo())
	                  : "raw return type matching in execution ok";
	
	// rule 4) A param type declared using a type variable is matched by its erasure
	declare warning : execution(Generic.new(Object)) 
	  : "erasure type matching in execution ok";
	declare warning : execution(Object Generic.foo())
	 : "erasure type matching in execution ok";
	
	// rule 5) no join points in bridge methods
	declare warning : execution(void UglyBuilding.iSee(String))
	                  : "execution and parameterized method ok";
	declare warning : execution(* ISore.*(..))
	                  : "execution and generic interface ok";
	declare warning : execution(* I2.*(..))
	                  : "execution and interface control test";
	declare warning : execution(void UglyBuilding.iSee(Object))
	                  : "should be no join points for bridge methods";
	
	// rule 6) parameterized types in return and args can be matched exactly
	declare warning : execution(Generic.new(List<String>)) : "match on parameterized args";
	declare warning : execution(List<Number> *(..)) : "match on parameterized return type";
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