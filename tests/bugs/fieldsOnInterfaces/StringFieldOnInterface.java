
import org.aspectj.testing.Tester;

/** @testcase PR#52107 declare String field on interface */
public class StringFieldOnInterface implements Runnable {
     public static void main(String[] args) {
         Tester.expectEvent("length=1");
         new StringFieldOnInterface().blah();
         Tester.checkAllEvents();
     }
     public void run() {
     }
}

aspect A {
     public String Runnable.name = "a";
     public void Runnable.blah() {
         int i = name.length();
         Tester.event("length="+i);
     }
}