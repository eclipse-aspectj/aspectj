import org.aspectj.testing.Tester; 

// bart.vanhaute@cs.kuleuven.ac.be conflated jitterbug
public class LocalClassClosingOverProceed {
    public static void main(String[] args) {
        new Target().method();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEvent("Target.method");
        Tester.expectEvent("before proceed");
        Tester.expectEvent("after proceed");
    }
}

class Target {
    public void method() {
        Tester.event("Target.method");
    }
}

aspect NamedLocalClass {

    /** @testcase PR#636 named local class closing over proceed() in around */
  void around(): execution(void Target.method())  {
      //Runnable r = new Runnable () {  
      class LocalClass {  
          public void run() {
              Tester.event("before proceed");
              proceed();
              Tester.event("after proceed");
          }
      };
      new LocalClass().run();
      //r.run();
  }
}
