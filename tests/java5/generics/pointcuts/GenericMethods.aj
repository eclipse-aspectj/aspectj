import java.util.*;

public aspect GenericMethods {
	
	declare warning : execution(Object whoAmI(Object))
	                  : "static generic method match";
	
	declare warning : withincode(Number myFavourite(List))
	                  : "instance generic method match";
	
	// should we have an XLint to explain why the pointcut below does not match?
	declare warning : execution(* myFavourite(List<Number>))
	                  : "no match because erasure is List";
	
}

class PlainClassWithGenericMethods {
	
	public static <T> T whoAmI(T t) {
		return t;
	}
	
	public <S extends Number> S myFavourite(List<S> ls) {
		return ls.get(0);
	}
	
	
}

class GenericClassWithGenericMethods<N> {
	
	N n;
	
	public static <T> T whoAmI(T t) {
		return t;
	}
	
	public <S extends Number> S myFavourite(List<S> ls) {
		return ls.get(0);
	}
		
}