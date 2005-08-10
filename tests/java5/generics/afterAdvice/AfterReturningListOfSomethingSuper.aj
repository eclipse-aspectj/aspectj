import java.util.*;

public aspect AfterReturningListOfSomethingSuper {
	
	/*
 	 *   - returning(List<? super Number>) matches List<Object>, List<Number>
     *	                              does not match List<Double>
	 *                                matches List, List<?> with unchecked warning
	 *                                matches List<? super Number>
	 *                                matches List<? extends Number> with unchecked warning
	 */
	
	List rawList(List l) { return l; }
	List<Object> listOfObject(List<Object> ls) { return ls; }
	List<Number> listOfNumber(List<Number> ln) { return ln; }
	List<Double> listOfDouble(List<Double> ld) { return ld; }
	List<?> listOfSomething(List<?> ls) { return ls; }
	List<? super Number> listOfSomethingSuper(List<? super Number> ln) {return ln; }
	List<? extends Number> listOfSomethingExtendsNumber(List<? extends Number> ln) { return ln; }
	
	public static void main(String[] args) {
		AfterReturningListOfSomethingSuper a = AfterReturningListOfSomethingSuper.aspectOf();
		a.rawList(new ArrayList());
		a.listOfObject(null);
		a.listOfNumber(null);
		a.listOfDouble(null);
		a.listOfSomething(new ArrayList());
		a.listOfSomethingSuper(null);
		a.listOfSomethingExtendsNumber(new ArrayList<Double>());
	}
	
	after() returning(List<? super Number> ln) : execution(* *(..)) {
		System.out.println("List<? super Number> matches " + thisJoinPointStaticPart);
	}
	
}