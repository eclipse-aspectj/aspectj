abstract aspect GenericAbstractAspect<T>{
	abstract protected pointcut pc();
	before() : pc() {}
}

aspect Concrete extends GenericAbstractAspect<Concrete> {
	// should get circular dependency error message from this
	protected pointcut pc() : pc();
}

aspect Concrete2 extends GenericAbstractAspect<Concrete2> {
	// this  should compile as expected
	protected pointcut pc() : p1();	
	pointcut p1() : call(void Concrete2.foo(..));
}

aspect Concrete3 extends GenericAbstractAspect<Concrete3> {
	// should get circular dependency error message from this
	protected pointcut pc() : pc1();
	pointcut pc1() : pc2();
	pointcut pc2() : pc();
}
