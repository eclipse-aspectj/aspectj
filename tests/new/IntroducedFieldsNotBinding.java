
import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

public class IntroducedFieldsNotBinding {
     public static void main(String[]args) {
          TargetClass target = new TargetClass();
          // when the compiler passes the test, validate runtime
          int result = target.getField();
          Tester.checkEqual(result, 1, "1 != result: " + result);
          Tester.checkAllEvents();
     }
     static {
          Tester.event("execution of getField");
     }
}

class TargetClass { }

class TargetClass2 { }

aspect A {
     private String TargetClass2.s = "hi";
     private int TargetClass.field = 1;
     public int TargetClass.getField() { 
          int i = field;  // compiler error here

          String s = new TargetClass2().s;
          Tester.checkEqual(s, "hi");
          
          Runnable r = new Runnable() {
                     public void run() {
                          System.out.println("running: " + field);
                     }
                };
          r.run();

          return i ;  
     }
     after () 
          : execution(public int TargetClass.getField()) {
          Tester.expectEvent("execution of getField");
     }
}
