import org.aspectj.testing.Tester;

public class Driver {
  public static void main(String[] args) { test(); }
  
  public static void test() { 

	C c = new C();
	D d = new D();
	E e = new E();

	Tester.check( c instanceof A, "C should extend A");
	Tester.check( c instanceof B, "Declare parents threw away superclass info: C should extend B");
	
	Tester.check( d instanceof A, "D should extend A");
	Tester.check( e instanceof A, "E should extend A");
	   
  }
  
  
  static class A {};
  static class B extends A {};
  static class C extends B {};
  
  static class D {};
  static class E extends D {};
  
  static aspect Adoption {
  	declare parents : C extends A;
  	declare parents : D extends A;
  };
}

