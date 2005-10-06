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
		System.out.println("cflow-ok " + p + " " + q + " " + thisJoinPoint);
	}
	
	before(A a) : execution(* *(..)) && @annotation(a) && !execution(* toString()){
		System.out.println("@annotation-ok " + a + " " + thisJoinPoint);
	}
	
	before(A a) : execution(* *(..)) && @args(a) && !execution(* toString()){
		System.out.println("@args-ok " + a + " " + thisJoinPoint);
	}
	
	before(P p) : execution(* *(..)) && args(..,p) && !execution(* toString()){
		System.out.println("args-ok " + p + " " + thisJoinPoint);
	}
	
	before(Q q) : this(q) && execution(* *(..)) && !execution(* toString()){
		System.out.println("this-ok " + q + " " + thisJoinPoint);
	}
	
	before(P p) : target(p) && execution(* *(..)) && !execution(* toString()){
		System.out.println("target-ok " + p + " " + thisJoinPoint);
	}
	
	before(A a) : @this(a) && execution(* *(..)) && !execution(* toString()){
		System.out.println("@this-ok " + a + " " + thisJoinPoint);
	}
	
	before(A a) : @target(a) && execution(* *(..)) && !execution(* toString()){
		System.out.println("@target-ok " + a + " " + thisJoinPoint);
	}
	
	before(A a) : @within(a) && execution(* *(..)) && !execution(* toString()){
		System.out.println("@within-ok " + a + " " + thisJoinPoint);
	}
	
	before(A a) : @withincode(a) && get(* *) {
		System.out.println("@withincode-ok " + a + " " + thisJoinPoint);
	}
}

aspect Sub extends GA<X,Y,MyAnnotation> {

	before(MyAnnotation a) : execution(* bar(..)) && @annotation(a) && !execution(* toString()){
		System.out.println("@annotation-ok-sub " + a + " " + thisJoinPoint);
	}
}

public class GenericAspectRuntimePointcuts {
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
	
	public String toString() { return "an X"; }
}

@MyAnnotation("on Y")
class Y {
	X x;
	
	void foo(X x) {}
	
	@MyAnnotation
	X bar() { return this.x; }
	
	public String toString() { return "a Y"; }
}