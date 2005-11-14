abstract aspect GenericAbstractAspect<T> {
	abstract protected pointcut pc();
	before() : pc() {}
}

abstract aspect SubGenericAspect<T> extends GenericAbstractAspect<T> {
	abstract protected pointcut pc1();
	abstract protected pointcut pc3();

	protected pointcut pc() : pc1();
	protected pointcut pc2() : pc3();
}

// this should compile with no errors
aspect Concrete2 extends SubGenericAspect<String> {	
	protected pointcut pc() : pc1();
	protected pointcut pc1() :pc3();
	protected pointcut pc3() : execution(* *(String));
}

class C {
	
	public void method(String s) {	
	}
	
	public void method2(int i) {	
	}
}
