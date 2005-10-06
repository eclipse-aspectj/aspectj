import java.lang.annotation.*;


abstract aspect DAGenTest<X> {
	
	declare @type: X : @MyAnnotation;
	
	declare @method: * X.*(..) : @MyAnnotation;
	
	declare @constructor: X.new(..) : @MyAnnotation;
	
	declare @field: X X.* : @MyAnnotation; 
	
}

@interface MyAnnotation {}

class C {
	
	C c = null;
	
	public C() {}
	
	public void foo() {}
	
}

aspect Sub extends DAGenTest<C> {
	
	declare warning : staticinitialization(@MyAnnotation *) : "@type ok";
	declare warning : execution(@MyAnnotation *.new(..)) : "@constructor ok";
	declare warning : execution(@MyAnnotation * *.*(..)) : "@method ok";
	declare warning : set(@MyAnnotation * *) : "@field ok";
	
}