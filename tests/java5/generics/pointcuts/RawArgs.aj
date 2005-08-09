import java.util.*;
/**
 *   - args(List) matches List, List<T>, List<String>
 */
public aspect RawArgs {
	
	before() : args(List) && call(* *(..)) {
		System.out.println("args(List) match at " + thisJoinPointStaticPart);
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
	
	void foo(List<T> lt) {
		;
	}
	
	void bar(List<String> ls) {
		;
	}
	
	void tada(List l) {
		;
	}
	
	
}