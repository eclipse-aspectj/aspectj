import org.aspectj.testing.Tester; 

/** @testcase cflow cycles in advice from different aspects */
public class CflowCycles {
  public static void main( String args[] ) {
        Tester.expectEvent("target A1");
        Tester.expectEvent("target A2");
        new Target().run();
        Tester.checkAllEventsIgnoreDups();
  }
}

class Target {
    public void run(){ }
}

aspect A1 {
    pointcut TargetRunFlow () 
        // ok if no cflow: within(Target) && execution(* *(..)) && !within(A1+);
        : !within(A1+) && !preinitialization(new(..)) && !initialization(new(..))//cflow(within(Target) && execution(* *(..))) && !within(A1+)
        ;
    Object around () : TargetRunFlow() {
        Tester.event("target A1");
        return proceed();
    }
    // ok if in the same class
}

aspect A2 {
    pointcut TargetRun () 
        : within(Target) && execution(* *(..)) && !within(A2+);
        ;
    Object around () : TargetRun() {
        Tester.event("target A2");
        return proceed();
    }
}


