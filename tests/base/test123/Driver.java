import org.aspectj.testing.Tester;

public class Driver {
  public static void main(String[] args) { test(); }

  public static void test() {
      C1 c1 = new C1(333);
      Tester.checkEqual(c1.afterAdvises, 0, "C1(int)");
      c1 = new C1("asdfasdf");
      Tester.checkEqual(c1.afterAdvises, 1, "C1(String)");
      C2 c2 = new C2(333);
      Tester.checkEqual(c2.afterAdvises, 0, "C2(int) -> C1(String)");
      c2 = new C2("foo");
      Tester.checkEqual(c2.afterAdvises, 1, "C2(String) -> C1(int)");
      c2 = new C2("foo", 42);
      Tester.checkEqual(c2.afterAdvises, 1, "C2(String,int) -> C1(String)");
  }
}

class C1 {
  public int afterAdvises = 0;

  C1() { }
  C1(int i) { }
  C1(String s) { }
}

class C2 extends C1 {  

    C2(int i) {
        super("asdf");
    }

    C2(String s) {
        super(42); 
    }

    C2(String s, int i) {
        super(s); 
    }
}

aspect A {
    after(/*C1 c1*/) returning(C1 c1): /*target(c1) && */
                                     call(new(String, ..)) {
        c1.afterAdvises += 1; 
    }
}
