import pkg.*;
import org.aspectj.testing.Tester;

public class Driver {
  public static void main(String[] args) { test(); }

  public static void test() {
    Obj obj = new Obj();
    Tester.checkEqual(obj.m(), 3, "obj.m()");
  }
}
