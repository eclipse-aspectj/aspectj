import java.util.*;

public aspect ArgsListOfSomethingSuper {
	
	/*
 	 *   - args(List<? super Number>) matches List<Object>, List<Number>
     *	                              does not match List<Double>
	 *                                matches List, List<?> with unchecked warning
	 *                                matches List<? super Number>
	 *                                matches List<? extends Number> with unchecked warning
	 */
	
	void rawList(List l) {}
	void listOfObject(List<Object> ls) {}
	void listOfNumber(List<Number> ln) {}
	void listOfDouble(List<Double> ld) {}
	void listOfSomething(List<?> ls) {}
	void listOfSomethingSuper(List<? super Number> ln) {}
	void listOfSomethingExtendsNumber(List<? extends Number> ln) {}
	
	public static void main(String[] args) {
		ArgsListOfSomethingSuper a = ArgsListOfSomethingSuper.aspectOf();
		a.rawList(null);
		a.listOfObject(null);
		a.listOfNumber(null);
		a.listOfDouble(null);
		a.listOfSomething(null);
		a.listOfSomethingSuper(null);
		a.listOfSomethingExtendsNumber(null);
	}
	
	before() : execution(* *(..)) && args(List<? super Number>) {
		System.out.println("List<? super Number> matches " + thisJoinPointStaticPart);
	}
	
}