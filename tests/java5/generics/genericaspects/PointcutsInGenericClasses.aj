public class PointcutsInGenericClasses<T> {
	
	pointcut foo() : execution(* T.*(..));
	
	
}

aspect X {
	
	 declare warning : PointcutsInGenericClasses<C>.foo() : "a match";
	
	
}

class C {
	void bar() {}
}