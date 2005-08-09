import java.util.*; import java.io.*;

public class GenericWildcardsInSignatureMatching {
	
	List<?> aList = new ArrayList<String>();
	
	void foo(Map<? extends Number,List<List<String>>> map) {
		;
	}
	
	List<? super Double> findOne(List<? super Double> ld) {
		return ld;
	}
	
	void anyOrder(List<? super Double> l) {}
	
}


aspect WildcardMatcher {
	
	declare warning : set(List<?> *) : "set of a list";
	
	declare warning : execution(* foo(Map<? extends Number,List<List<String>>>))
	                  : "exact nested wildcard match";
	
	declare warning : execution(* foo(Map<? extends Object+,List<List+>>))
	                  : "wildcard nested wildcard match";
	
	declare warning : execution(List<? super Double> findOne(List<? super Double>))
	                  : "super";
	
	declare warning : execution(* anyOrder(List<? super Object+>))
	                  : "super wild match";

	declare warning : execution(* anyOrder(List<?>))
    				  : "no match - signature is different";

}