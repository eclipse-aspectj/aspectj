import org.aspectj.testing.Tester;

public class IllegalAdoption {
  
  static class A {};
  static class B extends A {};
  static class C extends B {};
  
  static class D {};
  static class E extends D {};
  
  static aspect Adoption {
	declare parents : E extends C; // should cause a compilation error
  }
}

