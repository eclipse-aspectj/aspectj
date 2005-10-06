abstract aspect Base<S,T> {
	
	declare warning : execution(S T.*(..)) : "base match";
	
}

abstract aspect Middle<X> extends Base<C,X> {
	
	declare warning : execution(C X.*(..)) : "middle match";
	
}

aspect Top extends Middle<B> {
	
	declare warning : execution(C B.*(..)) : "top match";
	
}

class C {}

class B {
	
	C getC() { return null; }
	
}