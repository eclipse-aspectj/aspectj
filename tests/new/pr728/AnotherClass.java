
import org.aspectj.testing.*;

/** @testcase PR#728 file order in type searching */
public class AnotherClass implements Interface {
  public static class InnerClass { }
    public static void main (String[] args) {
        String s = new AnotherClass.InnerClass().toString(); 
        Tester.expectEvent(s);
        Tester.checkAllEvents();
    } 
}

