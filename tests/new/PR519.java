
import org.aspectj.testing.Tester; 

/** @testcase PR#519 Exception thrown when planning advice */
public class PR519 {
    private static final boolean DO_OUTPUT;
    private static final String[] EXPECTED;
    public static void main(String[] args) {
        try {
            A.main(args);
            Tester.checkAllEvents();
        } catch (MyException e) {
            // expecting this
            log("caught expected exception " + e);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    public static void log(String s) {
        if (DO_OUTPUT) System.err.println(s);
        Tester.event(s);
    }

    static {
        DO_OUTPUT = false;
        EXPECTED = new String[]
        { "*** class A.foo()"
          , "*** class B.foo()"
          , "*** class A.bar()"
          , "after():  call(void A.bar(..)) : call(void A.bar())"
          , "*** class A.bar()"
          , "after():  call(void A.bar(..)) : call(void A.bar())"
          , "*** class B.bar()"
          , "*** class A.bar(String s)"
          , "after():  call(void A.bar(..)) : call(void A.bar(String))"
          , "*** class A.bar(String s1, String s2)"
          , "after():  call(void A.bar(..)) : call(void A.bar(String, String))"
          , "*** class A.bar(int i)"
          , "*** class A.bar()"
          , "after():  call(void A.bar(..)) : call(void A.bar())"
          , "after():  call(void A.bar(..)) : call(void A.bar(int))"
          , "after():  call(void A.bar(..))  && args(problem): call(void A.bar(int))"
          , "caught expected exception MyException" };
        Tester.expectEvent(EXPECTED);
    } // static init
}

class A extends B {
    private int a = 0;
    private int lala;

    public void bar(int i){
        PR519.log("*** " + this.getClass() + ".bar(int i)");
        bar();
    }

    public void bar(){
        PR519.log("*** " + this.getClass() + ".bar()");
    }

    public void bar(String s){
        PR519.log("*** " + this.getClass() + ".bar(String s)");
    }
    public void bar(String s1, String s2){
        PR519.log("*** " + this.getClass() + ".bar(String s1, String s2)");
    }

    public static void main(String[] argv) throws Exception {
        B b = new B();
        A a = new A();

        a.foo();
        b.foo();

        a.bar();
        a.bar();
        b.bar();

        a.bar("lala");
        a.bar("lala", "poo");

        a.bar(9);
        throw new MyException();
    }
}
class MyException extends Exception { }
 
class B {
    private int b = 0;

    public void foo(){
        PR519.log("*** " + this.getClass() + ".foo()");
    }

    public void bar(){
        PR519.log("*** " + this.getClass() + ".bar()");
    }
}


aspect InstanceOfProblemAspect {
    public static final String PREFIX = "after():  call(void A.bar(..)) ";
    after(): !within(PR519) && call(void A.bar(..)){
        PR519.log(PREFIX + ": " + thisJoinPoint);
    }

    after(int problem): !within(PR519) && call(void A.bar(..)) && args(problem){
        PR519.log(PREFIX + " && args(problem): " + thisJoinPoint);
    }

}
