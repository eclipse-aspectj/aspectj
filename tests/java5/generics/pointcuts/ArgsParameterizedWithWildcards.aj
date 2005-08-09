import java.util.*;

public aspect ArgsParameterizedWithWildcards {

	/*
	 *   - args(List<Double>) matches List, List<?>, List<? extends Number> with unchecked warning
	 *                        matches List<Double>, List<? extends Double> ok (since Double is final)
	 */

	before() : execution(* *(..)) && args(List<Double>) {
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
	
	void rawList(List l) {}
	
	void listOfSomething(List<?> ls) {}
	
	void listOfSomeNumber(List<? extends Number> ln) {}
	
	void listOfDouble(List<Double> ld) {}
	
	void listOfSomeDouble(List<? extends Double> ld) {}
	
	void listOfString(List<String> ls) {}
	
}