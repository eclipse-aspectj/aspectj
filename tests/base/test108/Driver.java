
import org.aspectj.testing.Tester;
/**
 * This does accessibility of class and class variables, both inherited
 * and non-inherited, in the body of a weave.
 */

public class Driver { 
  public static void main(String[] args) { test(); }
  public static void test() {
    Tester.checkEqual((new Bar()).m(), 10, "Bar.m");
  }
}

class Foo {  
  int fooVar = 1;
  public int getFooVar( ) { return fooVar; }
}

class Bar extends Foo {  
  int barVar = 2;
  int ans = 0;
  public int getBarVar() { return barVar; }
  public void setAns( int newAns ) { ans = newAns; }
  int m() {
    return ans;
  }
}

abstract aspect A {
  static int aVar = 3;
}

aspect B extends A {
  static int bVar = 4;

  before(Bar b): target(b) && call(* m(..)) {
      b.setAns(b.getFooVar() + b.getBarVar() + aVar + bVar);
  }
}
