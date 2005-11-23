import java.lang.reflect.*;
import org.aspectj.testing.Tester;

public class OddConstructors {
    public static void main(String[] args) throws Exception { test(); }
     public static void test() throws Exception {
         new OddConstructors().go();
	 Tester.check("advised default constructor");
	 Tester.checkEqual(B.aspectOf().count, 1, "new'd once");
     }

     void go() throws Exception {
	 //         new C();

         // use reflection instead of new to create this class to tickle the bug
	 Constructor c = Class.forName("C").getConstructor(new Class[0]);
	 I i = (I)c.newInstance(new Object[0]);
     }
     static aspect B extends A issingleton() { //of eachJVM() {
         pointcut i(): target(I);
     }
}


abstract aspect A {

     abstract pointcut i();

     pointcut j(): 
	 i()
         // 2001.08.01 (palm)
         // Had to change this to I.new from new
         // because of the change to the meaning
         // of initialization
	 //&& initialization(new(..)) ;
         && initialization(I.new(..)) ;

     after() returning: j() {
	 Tester.note("advised default constructor");
	 count++;
     }

     int count = 0;
}

class C implements I { public C() {} }

interface I {}
