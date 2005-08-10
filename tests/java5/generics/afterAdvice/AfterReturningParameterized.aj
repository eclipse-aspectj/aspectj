import java.util.*;
/**
 *   - returning(List<String>) matches List<String> but not List<Number>
 *   - returning(List<String>) matches List with unchecked warning
 *   - returning(List<String>) matches List<?> with unchecked warning
 *   - returning(List<String>) matches List<T> with unchecked warning
 *   - returning(List<String>) matches List<T extends String> (String is final)
 */
public aspect AfterReturningParameterized {

	public static void main(String[] args) {
		 List<String> ls = new ArrayList<String>();
//		 ls.add("one");
//		 ls.add("two");
//		 ls.add("three");
		 
		 Generic<String> g = new Generic<String>();
		 g.foo(ls);
		 g.bar(ls);
		 g.tada(ls);
		 g.afar(new ArrayList<Number>());
		 g.something(ls);
		 
		 MustBeString<String> mbs = new MustBeString<String>();
		 mbs.listit(ls);
	 }
	
	after() returning(List<String> ls) : call(* *(..))  {
		System.out.println("returning(List<String> matched at " + thisJoinPointStaticPart);
		ls.add("four");
		String s = ls.get(0);
	}
	
}

class Generic<T> {
	
	List<T> foo(List<T> lt) {
		return lt;
	}
	
	List<String> bar(List<String> ls) {
		return ls;
	}
	
	List tada(List l) {
		return l;
	}
	
	List<Number> afar(List<Number> ln) {
		return ln;
	}
	
	List<?> something(List<?> ls) {
		return ls;
	}
}

class MustBeString<T extends String> {
	
	List<T> listit(List<T> lt) {
		return lt;
	}
	
}