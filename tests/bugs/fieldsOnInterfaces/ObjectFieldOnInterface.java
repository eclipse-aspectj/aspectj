
import org.aspectj.testing.Tester;

/** @testcase PR#52107 declare Object field on interface */
public class ObjectFieldOnInterface implements Runnable {
     public static void main(String[] args) {
         Tester.expectEvent("class(A): java.lang.Object");
         Tester.expectEvent("class(R): java.lang.Object");
         ObjectFieldOnInterface test
            = new ObjectFieldOnInterface();
         test.blah();
         Tester.event("class(R): " + test.object.getClass().getName());
         Tester.checkAllEvents();
     }
     public void run() {
     }
}

aspect A {
     public Object Runnable.object = new Object();
     public void Runnable.blah() {
         Tester.event("class(A): " + object.getClass().getName());
     }
}