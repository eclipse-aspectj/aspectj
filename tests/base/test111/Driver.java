import org.aspectj.testing.Tester;

public class Driver {
  public static void main(String[] args) { test(); }
  
  public static void test() {
    SubClass sub = new SubClass(3);
    //System.out.println("Invoking SubClass's getX method");
    Tester.checkEqual(sub.getX(), 3+42, "x value");
  }
}
