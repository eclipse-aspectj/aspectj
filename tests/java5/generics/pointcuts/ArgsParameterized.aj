import java.util.*;
/**
 *   - args(List<String>) matches List<String> but not List<Number>
 *   - args(List<String>) matches List with unchecked warning
 *   - args(List<String>) matches List<?> with unchecked warning
 *   - args(List<String>) matches List<T> with unchecked warning
 *   - args(List<String>) matches List<T extends String> (String is final)
 */
public aspect ArgsParameterized {

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
	
	before(List<String> ls) : call(* *(..)) && args(ls) {
		System.out.println("args(List<String> matched at " + thisJoinPointStaticPart);
		ls.add("four");
		String s = ls.get(0);
	}
	
}

class Generic<T> {
	
	void foo(List<T> lt) {
		;
	}
	
	void bar(List<String> ls) {
		;
	}
	
	void tada(List l) {
		;
	}
	
	void afar(List<Number> ln) {
		;
	}
	
	void something(List<?> ls) {
		;
	}
}

class MustBeString<T extends String> {
	
	void listit(List<T> lt) {
		;
	}
	
}