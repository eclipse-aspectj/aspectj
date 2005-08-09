import java.util.*;

public aspect ArgsListOfSomethingExtends {
	
	 // args(List<? extends Number) matches List<Number>, List<Double>, not List<String>
	 //                             matches List, List<?> with unchecked warning

	void rawList(List l) {}
	void listOfString(List<String> ls) {}
	void listOfNumber(List<Number> ln) {}
	void listOfDouble(List<Double> ld) {}
	void listOfSomething(List<?> ls) {}
	void listOfSomethingExtends(List<? extends Number> ln) {}
	void listOfSomethingSuper(List<? super Double> ln) {}
	
	public static void main(String[] args) {
		ArgsListOfSomethingExtends a = ArgsListOfSomethingExtends.aspectOf();
		a.rawList(null);
		a.listOfString(null);
		a.listOfNumber(null);
		a.listOfDouble(null);
		a.listOfSomething(null);
		a.listOfSomethingExtends(null);
		a.listOfSomethingSuper(null);
	}
	
	before() : execution(* *(..)) && args(List<? extends Number>) {
		System.out.println("List<? extends Number> matches " + thisJoinPointStaticPart);
	}
	
}