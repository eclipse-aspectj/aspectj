public class PR333 {
  public static void main(String[] args) {
      org.aspectj.testing.Tester.check(false, "Shouldn't have compiled");
  }
}
class A implements B {}
class B {}
