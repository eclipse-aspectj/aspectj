public class PointcutInGenericClassExample<T> {
	
	public pointcut foo() : execution(* T.*(..));
	
}

aspect A {
	
	declare warning : PointcutInGenericClassExample<C>.foo()
	                  : "parameterized with C";

	declare warning : PointcutInGenericClassExample<D>.foo()
					  : "parameterized with D";
	
//	declare warning : PointcutInGenericClassExample.foo()
//    				  : "raw";
	

}

class C {
	
	void bar() {}
	
}

class D {
	
	void goo() {}
	
}