// pushes the limits of what ajc will accept as an unambiguous binding...

aspect AmbiguousBindings {
	
	pointcut p1(Foo foo) : (call(* *(..)) && this(foo)) || (execution(* *(..)) && args(foo)); 
	
	pointcut p2(Foo foo) : (call(* m(..)) && this(foo)) || (call(* n(..)) && args(foo));
	
	pointcut p3(Foo foo) : (execution(* *(int,int)) && this(foo)) || (execution(* *(int)) && this(foo));
	
	pointcut p4(Foo foo) : (get(int a) && this(foo)) || (get(int b) && target(foo));
	
	pointcut p5(int x) : (set(int a) && args(x)) || (set(int b) && args(x));
	
	pointcut p6(Foo foo) : (within(Foo) && this(foo)) || (within(AmbiguousBindings) && args(foo));

	pointcut q1(Foo foo) : (call(* m(..)) && this(foo)) || (call(* m*(..)) && args(foo));
	
	pointcut q2(Foo foo) : (execution(* *(int,int)) && this(foo)) || (execution(* *(int,*)) && args(foo));
	
	pointcut q3(Foo foo) : (get(int a) && this(foo)) || (get(int a) && target(foo));
	
	pointcut q4(int x) : (set(int a) && args(x)) || (set(* *) && this(x));
	
	pointcut q5(Foo foo) : (within(Foo) && this(foo)) || (within(F*) && args(foo));

	// these should be good
	before(Foo foo) : p1(foo) {}
	before(Foo foo) : p2(foo) {}
	before(Foo foo) : p3(foo) {}
	before(Foo foo) : p4(foo) {}
	before(int z) : p5(z) {}
	before(Foo foo) : p6(foo) {}
	
	// these are all ambiguous
	before(Foo foo) : q1(foo) {}
	before(Foo foo) : q2(foo) {}
	before(Foo foo) : q3(foo) {}
	before(int x) : q4(x) {}
	before(Foo foo) : q5(foo) {}
	
}


class Foo {
	
	int a;
	int b;
	
	public void m(int x, int y) {
		a = y;
		n(x);
	}
	
	public void n(int x) {
		b = x;
		a = a * 2;
	}
	
}