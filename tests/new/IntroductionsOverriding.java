import org.aspectj.testing.*;

/** @testcase PR#654 Overriding method implementations using introduction on interfaces */
public class IntroductionsOverriding {
  public static void main (String [] args) {
    (new BaseClass ()).foo("Base");
    (new DerivedClass ()).foo("Derived");
    Tester.checkAllEvents();
  }
    static {
      Tester.expectEvent("Derived.foo(\"Derived\")");
      Tester.expectEvent("Base.foo(\"Base\")");
      Tester.expectEvent("around call Base");
      Tester.expectEvent("around execution Base");
      Tester.expectEvent("around call Base");       // called for both Base+ and Derived+
      Tester.expectEvent("around execution Base");
      Tester.expectEvent("around call Derived");
      Tester.expectEvent("around execution Derived");
    }
}

interface Base { public void foo(String s); }
interface Derived extends Base { public void foo(String s); } 
class DerivedClass implements Derived { }
class BaseClass implements Base { }

aspect A {
    public void Base.foo (String arg) {
        Tester.check("Base".equals(arg), 
                     "Base.foo(\"" + arg + "\")");
        Tester.event("Base.foo(\"" + arg + "\")"); 
    }
    public void Derived.foo (String arg) { 
        Tester.check("Derived".equals(arg), 
                     "Derived.foo(\"" + arg + "\")");
        Tester.event("Derived.foo(\"" + arg + "\")"); 
    }

    void around (Base o) 
        :  execution (void Base+.foo(..))  // ok if replacing call with execution
        && target (o) {
        Tester.event("around execution Base");
        proceed(o);
    }
    void around (Base o) 
        :  call (void Base+.foo(..))   
        && target (o) {
        Tester.event("around call Base");
        proceed(o);
    }
    void around () :  call (void Derived+.foo(..)) {
        Tester.event("around call Derived");
        proceed();
    }
    void around () :  execution (void Derived+.foo(..)) {
        Tester.event("around execution Derived");
        proceed();
    }
}
