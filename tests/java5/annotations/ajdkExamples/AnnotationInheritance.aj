	import java.lang.annotation.*;
	
	class C1 {
	  @SomeAnnotation
	  public void aMethod() {}
	}
	
	class C2 extends C1 {
	  public void aMethod() {}
	}
	
	class Main {
	  public static void main(String[] args) {
	    C1 c1 = new C1();
	    C2 c2 = new C2();
	    c1.aMethod();
	    c2.aMethod();
	  }
	}
	
	aspect X {
	
	  pointcut annotatedC2MethodCall() : 
	    call(@SomeAnnotation * C2.aMethod());  // matches nothing
	
	  pointcut annotatedMethodCall() :   // CW L16
	    call(@SomeAnnotation * aMethod());
	  
	  declare warning : annotatedC2MethodCall() : "annotatedC2MethodCall()";
	  declare warning : annotatedMethodCall() : "annotatedMethodCall()";
	}
	
	@Inherited @interface SomeAnnotation {}