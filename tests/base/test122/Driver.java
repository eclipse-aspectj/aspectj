import org.aspectj.testing.Tester;

public class Driver {
  public static void main(String[] args) { test(); }

  public static void test() {
    I i = new C4();
    Tester.checkEqual(i.m(), 42, "i.m()");
  }
}


interface I {}
abstract class C1 implements I{}
class C2 extends C1 implements I {}
class C3 extends C1 {}
class C4 extends C2 {}


aspect Aspect {
    //introduction I {
    public int I.m() {return 42;}
        //}
}
