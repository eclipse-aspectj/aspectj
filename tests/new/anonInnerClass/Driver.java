
import org.aspectj.testing.Tester;

// PR#297 anonymous inner class with aspect

public class Driver {

    public static String s = "";

    public static void main(String[] args){
        new Test().go();
        Tester.checkEqual(s, "-bar-after", "");
    }

}

class Test {
  void go(){
    seto( new I(){
       public void foo(){ bar(); }
    });
    o.foo();
  }

  void bar(){
    Driver.s += "-bar";
  }
    I o;

  void seto(I o){
    this.o = o;
  }
}

interface I { void foo(); }

aspect A  {
  void around(): this(I) && target(Test) && within(Test) && call(* bar()){
    proceed();
    Driver.s += "-after";
  }
}
