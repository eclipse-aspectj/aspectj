package javax.foo;

public class A {
  public static void main(String []argv) throws Exception {
    new A().foo();
    new B().foo();
  }

  public void foo() {
	  System.out.println("(A) method running");
  }
}
