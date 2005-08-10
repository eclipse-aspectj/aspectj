import java.util.*;

public aspect AfterReturningListOfSomethingExtends {
	
	 // returning(List<? extends Number) matches List<Number>, List<Double>, not List<String>
	 //                             matches List, List<?> with unchecked warning

	List rawList(List l) { return l; }
	List<String> listOfString(List<String> ls) { return ls; }
	List<Number> listOfNumber(List<Number> ln) { return ln; }
	List<Double> listOfDouble(List<Double> ld) { return ld; }
	List<?> listOfSomething(List<?> ls) { return ls; }
	List<? extends Number> listOfSomethingExtends(List<? extends Number> ln) { return ln; }
	List<? super Double> listOfSomethingSuper(List<? super Double> ln) { return ln; }
	
	public static void main(String[] args) {
		AfterReturningListOfSomethingExtends a = AfterReturningListOfSomethingExtends.aspectOf();
		a.rawList(new ArrayList());
		a.listOfString(null);
		a.listOfNumber(null);
		a.listOfDouble(null);
		a.listOfSomething(new ArrayList());
		a.listOfSomethingExtends(null);
		a.listOfSomethingSuper(null);
	}
	
	after() returning(List<? extends Number> ln) : execution(* *(..)) {
		System.out.println("List<? extends Number> matches " + thisJoinPointStaticPart);
	}
	
}