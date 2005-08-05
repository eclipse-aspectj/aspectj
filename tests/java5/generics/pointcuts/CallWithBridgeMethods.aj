/**
 * 
 * Each method T m(T1,...Tn) throws S1,...Sm is translated to a method with the same name
 * whose return type, argument types, and thrown types are the erasures of the corresponding
 * types in the original method. In addition, if a method m of a class or interface C is 
 * inherited in a subclass D, a bridge method might need to be generated in D. The rules are
 * as follows:
 * 
 * If C.m is directly overridden by a method D.m in D, and the erasure of the return type or
 * argument types of D.m differs from the erasure of the corresponding types in C.m, a bridge
 * method needs to be generated.
 * 
 */


public aspect CallWithBridgeMethods {
	
	void bar() {
		SubGeneric rawType = new SubGeneric();
		Generic rawVariableOfSuperType = rawType;
		rawVariableOfSuperType.foo("hi");  // this call we go to the bridge method..., but
		                                   // appears in the bytecode as Generic.foo(Object)
		rawType.foo("hi");				   // this call will go to the bridge method, and
		                                   // appears in the bytecode as SubGeneric.foo(Object)
	}
	
	declare warning : call(* SubGeneric.foo(..)) && within(SubGeneric) 
	                  : "should not match call in bridge method";
	
	declare warning : call(* SubGeneric.foo(Object)) 
	                  : "should match call to bridge method on L23, this is a real call!";
	
	declare warning : execution(* SubGeneric.foo(Object)) && within(SubGeneric)
	                  : "but whilst you can call it, it doesn't execute!";
}


class Generic<T> {
	
	public T foo(T someObject) {
		return someObject;
	}
	
}

class SubGeneric<N extends Number> extends Generic<N> {
	
	public N foo(N someNumber) {
		return someNumber;
	}
	
	// "bridge" method is:
//	public Object foo(Object someObject) {
//		Number n = (Number)someObject;
//		return foo(n);
//	}
	
}

class SubSubGeneric<N extends Number> extends SubGeneric<N> {

	// inherits bridge method from super
	
	// NO bridge method generated (this is in contrast to the statements in the Bracha paper).
	// "Adding Generics to the Java Programming Language, Public Draft Specification v2.0",
	// Bracha et. al, June 23, 2003.

}


class SubGenericWithSameErasure<T> extends Generic<T> {
	
	// NO bridge method generated (this is in contrast to the statements in the Bracha paper).
	// "Adding Generics to the Java Programming Language, Public Draft Specification v2.0",
	// Bracha et. al, June 23, 2003.
	
}