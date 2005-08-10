import java.util.*;
/**
 *   - returning(List) matches List, List<T>, List<String>
 */
public aspect AfterReturningRawType {
	
	after() returning(List l) : call(* *(..)) {
		System.out.println("returning(List) match at " + thisJoinPointStaticPart);
	}
	
	public static void main(String[] args) {
		Generic<Double> g = new Generic<Double>();
		g.foo(new ArrayList<Double>());
		g.bar(new ArrayList<String>());
		g.tada(new ArrayList());
		g.tada(new ArrayList<String>());
		g.tada(new ArrayList<Double>());
	}
	
}

class Generic<T> {
	
	List<T> foo(List<T> lt) {
		return lt;
	}
	
	List<String> bar(List<String> ls) {
		return ls;
	}
	
	List tada(List l) {
		return l;
	}
	
	
}