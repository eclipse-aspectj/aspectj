/**
 * 
 * 
 */
aspect A dominates Foo persingleton() { }

//aspect B perthis(foo()) {
//XXX don't forget an error test for foo() not found}

aspect C {
	pointcut w(int x): args(x) && if(x < 10);
	pointcut x(): cflow(y());
	pointcut y(): withincode(int m());
	
	pointcut z(): execution(void m()) a b c;
	declare error: execution(void m()): "hi" ac;
	
	pointcut p(): handler(Foo);
	
	pointcut p2(): initialization(Object.new(..));
	
	declare dominates: A, B;
}

aspect D a b c {}