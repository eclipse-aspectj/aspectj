import java.lang.annotation.*;

abstract aspect GA<P,Q,A extends Annotation> {
	
	/*
		* test before advice with
	    - CflowPointcut
	    - annotation
	    - args annotation
	    - args
	    - this
	    - target
	    - @this
	    - @target
	    - @within
	    - @withincode
	    - parameter binding
	 */
	
	before(P p, Q q) : cflow(execution(* P.*(..)) && this(p)) && set(Q *) && args(q) {
		System.out.println("cflow-ok " + p + " " + q);
	}
	
	before(A a) : execution(* *(..)) && @annotation(a) {
		System.out.println("@annotation-ok " + a);
	}
	
	before(A a) : @args(a) {
		System.out.println("@args-ok " + a);
	}
	
	before(P p) : args(..,p) {
		System.out.println("args-ok " + p);
	}
	
	before(Q q) : this(q) && execution(* *(..)) {
		System.out.println("this-ok " + q);
	}
	
	before(P p) : target(p) && call(* *(..)) {
		System.out.println("target-ok " + p);
	}
	
	before(A a) : @this(a) && execution(* *(..)) {
		System.out.println("@this-ok " + a);
	}
	
	before(A a) : @target(a) && call(* *(..)) {
		System.out.println("@target-ok " + a);
	}
	
	before(A a) : @within(a) && execution(* *(..)) {
		System.out.println("@within-ok " + a);
	}
	
	before(A a) : @withincode(a) && get(* *) {
		System.out.println("@withincode-ok " + a);
	}
}

aspect GenericAspectRuntimePointcuts extends GA<X,Y,MyAnnotation> {
	
	public static void main(String[] s) {
		X x = new X();
		Y y = new Y();
		x.foo();
		x.bar();
		y.foo(x);
		y.bar();
	}
}

@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotation {
	String value() default "my-value";
}

@MyAnnotation
class X {
	Y y;
	
	void foo() {
		this.y = new Y();
	}
	
	@MyAnnotation("bar")
	void bar() {}
	
}

@MyAnnotation("on Y")
class Y {
	X x;
	
	void foo(X x) {}
	
	@MyAnnotation
	X bar() { return this.x; }
}