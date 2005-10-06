public class PointcutsInGenericClasses2<T> {
	
	pointcut foo() : execution(* T.*(..));
	
	
}

aspect X {
	
	 declare warning : PointcutsInGenericClasses2.foo() : "a match";
	
	
}

class C {
	void bar() {}
}