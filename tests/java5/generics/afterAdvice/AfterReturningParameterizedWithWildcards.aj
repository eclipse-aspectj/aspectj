import java.util.*;

public aspect AfterReturningParameterizedWithWildcards {

	/*
	 *   - returning(List<Double>) matches List, List<?>, List<? extends Number> with unchecked warning
	 *                        matches List<Double>, List<? extends Double> ok (since Double is final)
	 */

	after() returning(List<Double> ld) : call(* *(..)) {
		System.out.println("List<Double> matched at " + thisJoinPointStaticPart);
	}
	
	public static void main(String[] args) {
		C c = new C();
		List<Double> ld = new ArrayList<Double>();
		c.rawList(ld);
		c.listOfSomething(ld);
		c.listOfSomeNumber(ld);
		c.listOfDouble(ld);
		c.listOfSomeDouble(ld);
		c.listOfString(new ArrayList<String>());
	}
}

class C {
	
	List rawList(List l) { return l;}
	
	List<?> listOfSomething(List<?> ls) { return ls; }
	
	List<? extends Number> listOfSomeNumber(List<? extends Number> ln) { return ln; }
	
	List<Double> listOfDouble(List<Double> ld) { return ld; }
	
	List<? extends Double> listOfSomeDouble(List<? extends Double> ld) { return ld; }
	
	List<String> listOfString(List<String> ls) { return ls; }
	
}