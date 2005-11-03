import java.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import java.lang.reflect.*;

/**
 * test annotations on itds in various ways 
 * 1) annotated ITD
 * 2) binding of an annotation on an ITD member
 * 3) annotated ITD via declare @xxx
 * 4) binding of annotated ITD via declare
 */
public aspect AnnotationsAndITDs {

	// annotated ITD constructors
	
	@SomeAnnotation(s="hello",clazz=AnnotationsAndITDs.class)
	public ITDMe.new(String s) { this(); }
	
	@SomeAnnotation(s="goodbye",clazz=String.class)
	private ITDMe.new(int x) { this(); }

	// annotated ITD methods
	
	@SomeAnnotation(s="x",clazz=Object.class)
	private void ITDMe.foo() {}
	
	@SomeAnnotation(s="y",clazz=Integer.class)
	public void ITDMe.bar(@ParamAnnotation int x) {}

	// annotated ITD fields
	
	@SomeAnnotation(s="d",clazz=Double.class)
	public Double ITDMe.d;
	
	@SomeAnnotation(s="f",clazz=Double.class)
	private Float ITDMe.f;
	
	// declare @xxx on ITD members
	// =============================
	
	// annotated ITD constructors
	
	declare @constructor : ITDMe2.new(..) : @SomeAnnotation(s="@cons",clazz=String.class);
	
	public ITDMe2.new(String s) { this(); }	
	private ITDMe2.new(int x) { this(); }

	// annotated ITD methods
	
	declare @method : * ITDMe2.*(..) : @SomeAnnotation(s="@method",clazz=ITDMe2.class);
	
	private void ITDMe2.foo() {}	
	public void ITDMe2.bar(@ParamAnnotation int x) {}

	// annotated ITD fields
	
	declare @field : * ITDMe2.* : @SomeAnnotation(s="@field",clazz=ITDMe2.class);
	
	public Double ITDMe2.d = 2d;	
	private Float ITDMe2.f = 1.0f;
	
	declare @type : ITDMe* : @SomeAnnotation(s="@type",clazz=System.class);
	
	
	public static void main(String[] args) {
		ITDMe itdme1 = new ITDMe("foo");
		ITDMe itdme2 = new ITDMe(5);
		itdme1.foo();
		itdme1.bar(6);
		itdme1.d = 1.0d;
		itdme1.f = 1.0f;
		ITDMe2 itdme21 = new ITDMe2("foo");
		ITDMe2 itdme22 = new ITDMe2(5);
		itdme21.foo();
		itdme21.bar(6);
		itdme21.d = 1.0d;
		itdme21.f = 1.0f;
	}
}

aspect AnnotationTests {
	
	// static tests
	
	declare warning : execution(@SomeAnnotation * *(..)) : "execution(@SomeAnnotation ...)";
	
	declare warning : execution(@SomeAnnotation new(..)) : "execution(@SomeAnnotation ...new(..)";
	
	declare warning : set(@SomeAnnotation * *) : "set(@SomeAnnotation...)";
	
	declare warning : staticinitialization(@SomeAnnotation *) : "si(@SomeAnnotation...)";
	
	// binding tests
	
	before(SomeAnnotation sa) : execution(public ITDMe.new(String)) && @annotation(sa) {
		print(sa,thisJoinPoint.getSourceLocation().toString());
	}
	
	after(SomeAnnotation sa) : execution(private ITDMe.new(int)) && @annotation(sa) {
		print(sa,thisJoinPoint.getSourceLocation().toString());
	}
	
	before(SomeAnnotation sa) : execution(private ITDMe.new(int)) && @annotation(sa) {
		print(sa,thisJoinPoint.getSourceLocation().toString());
	}
	
	after(SomeAnnotation sa) : execution(public void ITDMe.bar(int)) && @annotation(sa) {
		print(sa,thisJoinPoint.getSourceLocation().toString());
//		MethodSignature sig = (MethodSignature) thisJoinPoint.getSignature(); 
//		Method meth = sig.getMethod();
//		Annotation[][] anns = meth.getParameterAnnotations();
//		System.out.println("method bar has " + anns.length + " params, first param annotation is " 
//				+ anns[0][0].toString());
	}
	
	before(SomeAnnotation sa) : set(public Double ITDMe.d) && @annotation(sa) {
		print(sa,thisJoinPoint.getSourceLocation().toString());
	}
	
	after(SomeAnnotation sa) : set(private Float ITDMe.f) && @annotation(sa) {
		print(sa,thisJoinPoint.getSourceLocation().toString());
	}
	
        after(SomeAnnotation sa) returning : staticinitialization(@SomeAnnotation *) && @annotation(sa){
		print(sa,thisJoinPoint.getSourceLocation().toString());
	}
	
	// now repeat for the @declared versions

	before(SomeAnnotation sa) : execution(public ITDMe2.new(String)) && @annotation(sa) {
		print(sa,thisJoinPoint.getSourceLocation().toString());
	}
	
	after(SomeAnnotation sa) : execution(private ITDMe2.new(int)) && @annotation(sa) {
		print(sa,thisJoinPoint.getSourceLocation().toString());
	}
	
	before(SomeAnnotation sa) : execution(private ITDMe2.new(int)) && @annotation(sa) {
		print(sa,thisJoinPoint.getSourceLocation().toString());
	}
	
	after(SomeAnnotation sa) : execution(public void ITDMe2.bar(int)) && @annotation(sa) {
		print(sa,thisJoinPoint.getSourceLocation().toString());
//		MethodSignature sig = (MethodSignature) thisJoinPoint.getSignature();
//		Method meth = sig.getMethod();
//		Annotation[][] anns = meth.getParameterAnnotations();
//		System.out.println("method bar has " + anns.length + " params, first param annotation is " 
//				+ anns[0][0].toString());
	}
	
	before(SomeAnnotation sa) : set(public Double ITDMe2.d) && @annotation(sa) {
		print(sa,thisJoinPoint.getSourceLocation().toString());
	}
	
	after(SomeAnnotation sa) : set(private Float ITDMe2.f) && @annotation(sa) {
		print(sa,thisJoinPoint.getSourceLocation().toString());
	}
		
	private void print(SomeAnnotation sa,String loc) {
		System.err.println(sa.s() + " " + sa.clazz().getName()+" ("+loc+")");
	}
}

@Retention(RetentionPolicy.RUNTIME)
@interface SomeAnnotation {
	String s();
	Class  clazz();
}

@Retention(RetentionPolicy.RUNTIME)
@interface ParamAnnotation{
	String value() default "";
}

class ITDMe {
	
	
}

class ITDMe2 {
	
	
}
