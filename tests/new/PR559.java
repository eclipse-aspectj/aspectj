import org.aspectj.testing.Tester; 

/**
   * This test case shows:
   * - no difference whether extending a class or aspect or interface
   * - underlying "within"-based pointcut works
   * - containing cflow-based pointcut does not
   * - true only of inherited pointcuts - member pointcuts work fine
   *
   * Note that to avoid pointcut cycles, 
   * pointcuts exclude code within a common superinterface.
   * It is not enough (now) to exclude code in the advice's super-most class subclasses.
   *
   * @testcase PR#559 subclass advice not run for join points selected by superclass cflow-based pointcuts
 */
public class PR559 {
  public static void main( String args[] ) {
        Tester.expectEvent("target aspect");
        Tester.expectEvent("inTarget class");
        Tester.expectEvent("inTarget aspect");
        Tester.expectEvent("inTargetFlow class");
        Tester.expectEvent("inTargetFlow aspect");
        Tester.expectEvent("TargetRun aspect");
        Tester.expectEvent("TargetRun class");
        Tester.expectEvent("TargetRunFlow aspect");
        Tester.expectEvent("TargetRunFlow class");
        Tester.expectEvent("TargetSubRunFlow aspect");
        Tester.expectEvent("TargetSubRunFlow class");
        new Target().run();
        Tester.checkAllEvents();
  }
}

interface AspectMarker {
    pointcut notInAspect()   : ! within(AspectMarker+) && 
    				!preinitialization(new(..)) && !initialization(new(..));
    pointcut allTarget()     : execution(* Target.*(..)) ;
    pointcut allTargetFlow() : cflow(allTarget()); 
    pointcut inTarget()      : notInAspect() && allTarget();
    pointcut inTargetFlow()  : notInAspect() && allTargetFlow();
}

class Target {
    public void run(){ }
}

class Base implements AspectMarker {
    pointcut TargetRun () 
        : within(Target) && execution(* *(..)) && notInAspect() ;
        ;
    pointcut TargetRunFlow () 
        : cflow(within(Target) && execution(* *(..))) && notInAspect() 
        ;
}

/** @testcase PR#559 subaspect advice not run for superclass cflow-based pointcut */
aspect Derived extends Base {
    pointcut TargetSubRunFlow () 
        : cflow(within(Target) && execution(* *(..))) && notInAspect() 
        ;
    Object around () : inTarget() {
        Tester.event("inTarget class");
        return proceed();
    }
    Object around () : inTargetFlow() {
        Tester.event("inTargetFlow class");
        return proceed();
    }
    Object around () : TargetRun() {
        Tester.event("TargetRun class");
        return proceed();
    }
    Object around () : TargetRunFlow() {
        Tester.event("TargetRunFlow class");
        return proceed();
    }
    Object around () : TargetSubRunFlow() {
        Tester.event("TargetSubRunFlow class");
        return proceed();
    }
}

abstract aspect BaseAspect implements AspectMarker {
    pointcut TargetRun () 
        : within(Target) && execution(* *(..)) && notInAspect() ;
        ;
    pointcut TargetRunFlow () 
        : cflow(within(Target) && execution(* *(..))) && notInAspect() 
        ;
}

/** @testcase PR#559 subaspect advice not run for superaspect cflow-based pointcut */
aspect DerivedAspect extends BaseAspect implements AspectMarker {
    pointcut TargetSubRunFlow () 
        : cflow(within(Target) && execution(* *(..))) && notInAspect() 
        ;
    Object around () : TargetRun() {
        Tester.event("target aspect");
        return proceed();
    }
    Object around () : inTarget() { // TargetRun() {
        Tester.event("inTarget aspect");
        return proceed();
    }
    Object around () : inTargetFlow() { // TargetRun() {
        Tester.event("inTargetFlow aspect");
        return proceed();
    }
    Object around () : TargetRun() {
        Tester.event("TargetRun aspect");
        return proceed();
    }
    Object around () : TargetRunFlow() {
        Tester.event("TargetRunFlow aspect");
        return proceed();
    }
    Object around () : TargetSubRunFlow() {
        Tester.event("TargetSubRunFlow aspect");
        return proceed();
    }
}

