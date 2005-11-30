public class A {
  public static void main(String []argv) {
    try {
      new A().foo();
    } catch (Exception e) {
    }
  }

  public void foo() {
    try {
    } catch (Exception e) {
    }
  }
}
 

aspect ComplexSub extends TrackingErrors {
  public pointcut errorScope(): within(A);
}
  
