import java.util.*;

public aspect AfterReturningListOfSomething {
	
	// returning(List<?>) matches List, List<String>, List<?>, ...
		
	List rawList(List l) { return l;}
	List listOfString(List<String> ls) { return ls; }
	List listOfSomething(List<?> ls) { return ls; }
	List listOfSomethingExtends(List<? extends Number> ln) { return ln; }
	List listOfSomethingSuper(List<? super Double> ln) { return ln; }
	
	// try a couple of nested variations too
	Map<List<String>,List<List<Float>>> mapit(Map<List<String>,List<List<Float>>> m) { return m;}
	HashSet<Double> setOf(HashSet<Double> sd) { return sd; }
	
	public static void main(String[] args) {
		AfterReturningListOfSomething a = AfterReturningListOfSomething.aspectOf();
		a.rawList(null);
		a.listOfString(null);
		a.listOfSomething(null);
		a.listOfSomethingExtends(null);
		a.listOfSomethingSuper(null);
		a.mapit(null);
		a.setOf(null);
	}
	
	after() returning(List<?> aList) : execution(* *(..)) {
		System.out.println("List<?> matches " + thisJoinPointStaticPart);
	}
	
	after() returning(Map<?,?> aMap) : execution(* *(..)) {
		System.out.println("wild map matches " + thisJoinPointStaticPart);
	}
	@org.aspectj.lang.annotation.SuppressAjWarnings
	after() returning(HashMap<List<?>,List<List<?>>> aMap) : execution(* *(..)) {
		System.out.println("nested wild map does not match " + thisJoinPointStaticPart);
	}
	
	after() returning(Map<List<String>,List<List<Float>>> aMap) : execution(* *(..)) {
		System.out.println("exact wild map matches " + thisJoinPointStaticPart);
	}
	
	after() returning(Set<Double> aSet) : execution(* *(..)) {
		System.out.println("super type exact matches " + thisJoinPointStaticPart);
	}
	
	after() returning(Set<?> aSet) : execution(* *(..)) {
		System.out.println("super wild type matches " + thisJoinPointStaticPart);
	}
}