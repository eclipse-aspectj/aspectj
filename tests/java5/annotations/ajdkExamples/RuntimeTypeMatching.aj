import java.lang.annotation.*;
import java.lang.reflect.*;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

public aspect RuntimeTypeMatching {
	
	public static void main(String[] args) {
		A a = new A();
		B b = new B();
		a.a();
		b.b();
		b.callA(a);
		ByeByeEJB pojo = new ByeByeEJB();
		pojo.method1();
		pojo.method2();
		pojo.method3();
	}
	
	after() returning : @this(Foo) && execution(* A+.*(..)) {
		System.out.println("@this(Foo) at " + thisJoinPoint.toString());
	}
	
	after() returning : call(* *(..)) && @target(Classified) {
		System.out.println("@target(Classified) at " + thisJoinPoint.toString());
	}
	
	pointcut callToClassifiedObject(Classified classificationInfo) :
  	    call(* *(..)) && @target(classificationInfo);
	
	before(Classified classification) : callToClassifiedObject(classification) {
		System.out.println("This information is " + classification.classification());
	}
	
  	pointcut txRequiredMethod(Tx transactionAnnotation) :
  	    execution(* *(..)) && @this(transactionAnnotation) 
  	    && if(transactionAnnotation.policy() == TxPolicy.REQUIRED);

  	before() : txRequiredMethod(Tx) {
  		System.out.println("(Class) Transaction required at " + thisJoinPoint);
  	}
  	
  	before(Tx tranAnn) : execution(* *(..)) && @annotation(tranAnn) && if(tranAnn.policy()==TxPolicy.REQUIRED) {
  		System.out.println("(Method) Transaction required at " + thisJoinPoint);  		
  	}
  	
  	/**
  	 * matches any join point with at least one argument, and where the
  	 * type of the first argument has the @Classified annotation
  	 */
  	pointcut classifiedArgument() : @args(Classified,..);
  	
  	before() : classifiedArgument() {
  		System.out.println("Classified data being passed at " + thisJoinPoint);
  	}
  	
  	/**
  	 * matches any join point with three arguments, where the third
  	 * argument has an annotation of type @Untrusted.
  	 */
  	pointcut untrustedData(Untrusted untrustedDataSource) : 
  	    @args(*,*,untrustedDataSource);
  	
  	before(Untrusted source) : untrustedData(source) {
  		System.out.println("Untrusted data being passed at " + thisJoinPoint);
  		if (source == null) System.out.println("FAIL");
  	}
  	
  	before() : execution(* callA(..)) {
  	  	Annotation[] thisAnnotations = thisJoinPoint.getThis().getClass().getAnnotations();
  	  	Annotation[] targetAnnotations = thisJoinPoint.getTarget().getClass().getAnnotations();
  	  	Annotation[] firstParamAnnotations = thisJoinPoint.getArgs()[0].getClass().getAnnotations();
 
  	  	System.out.println(thisAnnotations.length + " " + thisAnnotations[0].toString());
  	  	System.out.println(targetAnnotations.length + " " + targetAnnotations[0].toString());
  	  	System.out.println(firstParamAnnotations.length + " " + firstParamAnnotations[0].toString());
  	  	
  	}
  	
  	// up to @within and @withincode examples
  	declare warning : @within(Foo) && execution(* *(..)) : "@within(Foo)";
  	
  	pointcut insideCriticalMethod(Critical c) : @withincode(c);
  	
  	before(Critical c) : insideCriticalMethod(c) {
  		System.out.println("Entering critical join point with priority " + c.priority());
  	}
  	
  	before() : insideCriticalMethod(Critical) {
  		Signature sig = thisEnclosingJoinPointStaticPart.getSignature();
  	  	AnnotatedElement declaringTypeAnnotationInfo = sig.getDeclaringType();
  	  	if (sig instanceof MethodSignature) {
  	  	  // this must be a call or execution join point.
  	  	  Method method = ((MethodSignature)sig).getMethod();
  	  	  Critical c = method.getAnnotation(Critical.class);
  	  	  System.out.println("Entering critical join point with reflectively obtained priority " + c.priority());
  	  	}
  	}
  	
}

@Retention(RetentionPolicy.RUNTIME) @interface Foo {}
@Retention(RetentionPolicy.RUNTIME) @interface Classified {
	String classification() default "TOP-SECRET";
}
@Retention(RetentionPolicy.RUNTIME) @interface Untrusted {}
@Retention(RetentionPolicy.RUNTIME) @interface Critical {
	int priority() default  5;
}

enum TxPolicy { REQUIRED, REQUIRESNEW }
@Retention(RetentionPolicy.RUNTIME) @interface Tx {
	TxPolicy policy() default  TxPolicy.REQUIRED;
}

@Classified class A {
	void a() {};
}

@Foo class B extends A {
	void b() {};
	@Critical(priority=3) void callA(A a) { a.a(); }
}

@Tx(policy=TxPolicy.REQUIRED)
class ByeByeEJB {
	@Tx void method1() {}
	@Tx(policy=TxPolicy.REQUIRED) void method2() {}
	@Tx(policy=TxPolicy.REQUIRESNEW) void method3() {}
}

@Untrusted class Dodgy {}

class ToTrustOrNot {
	
	void a() {
		b(5,2,new Dodgy());
		b(5,2,new String());
	}
	
	void b(int x, int y, Object o) {}
}