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
	
	  pointcut annotatedMethodCall() : 
	    call(@SomeAnnotation * C1.aMethod());  //CW L16
	
	  pointcut c1MethodCall() :   // CW L16, L17
	    call(* C1.aMethod());
	  
	  declare warning : annotatedMethodCall() : "annotatedMethodCall()";
	  declare warning : c1MethodCall() : "c1MethodCall()";
	}
	
	@Inherited @interface SomeAnnotation {}