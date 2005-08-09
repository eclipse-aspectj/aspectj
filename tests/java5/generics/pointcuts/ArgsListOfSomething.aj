import java.util.*;

public aspect ArgsListOfSomething {
	
	// args(List<?>) matches List, List<String>, List<?>, ...
		
	void rawList(List l) {}
	void listOfString(List<String> ls) {}
	void listOfSomething(List<?> ls) {}
	void listOfSomethingExtends(List<? extends Number> ln) {}
	void listOfSomethingSuper(List<? super Double> ln) {}
	
	// try a couple of nested variations too
	void mapit(Map<List<String>,List<List<Float>>> m) {}
	void setOf(HashSet<Double> sd) {}
	
	public static void main(String[] args) {
		ArgsListOfSomething a = ArgsListOfSomething.aspectOf();
		a.rawList(null);
		a.listOfString(null);
		a.listOfSomething(null);
		a.listOfSomethingExtends(null);
		a.listOfSomethingSuper(null);
		a.mapit(null);
		a.setOf(null);
	}
	
	before() : execution(* *(..)) && args(List<?>) {
		System.out.println("List<?> matches " + thisJoinPointStaticPart);
	}
	
	before() : execution(* *(..)) && args(Map<?,?>) {
		System.out.println("wild map matches " + thisJoinPointStaticPart);
	}
	@org.aspectj.lang.annotation.SuppressAjWarnings
	before() : execution(* *(..)) && args(HashMap<List<?>,List<List<?>>>) {
		System.out.println("nested wild map does not match " + thisJoinPointStaticPart);
	}
	
	before() : execution(* *(..)) && args(Map<List<String>,List<List<Float>>>) {
		System.out.println("exact wild map matches " + thisJoinPointStaticPart);
	}
	
	before() : execution(* *(..)) && args(Set<Double>) {
		System.out.println("super type exact matches " + thisJoinPointStaticPart);
	}
	
	before() : execution(* *(..)) && args(Set<?>) {
		System.out.println("super wild type matches " + thisJoinPointStaticPart);
	}
}