import org.aspectj.testing.Tester; 

/**
 * @testcase PR#560 compile fails for aspect derived from percflow base aspect unless pointcut excludes base aspect and subaspects
 * - works with issingleton perthis and pertarget 
 * - works when advice is in the same class/aspect 
 */
public class PR560 { // XXX broken?
  public static void main( String args[] ) {
        Tester.expectEvent("Target.run()");
        Tester.expectEvent("same aspect");
        Tester.expectEvent("derived aspect");
        new Target().run();
        Tester.checkAllEventsIgnoreDups();
  }
}

class Target {
    public void run(){
        Tester.event("Target.run()");
    }
}
abstract aspect Base percflow(callcflow()) {
    pointcut callcflow() 
        : cflow(call(public void Target.run()))
        && !within(Base+)
        ;
    before() : within(Target) {
        Tester.event("same aspect");
    }
}

aspect Derived extends Base { 
    before() : within(Target) {
        Tester.event("derived aspect");
    }
}

/*
  -- passing variants
  -
        : cflow(call(public void Target.run()))
        && !within(Base+)

  -- failing variants
  -
        : cflow(call(public void Target.run()) && !within(Derived)) 
          (cflowbelow selects Derived.*)
  -
        : cflow(call(public void Target.run()))
        && !cflow(within(Derived))
          (cflowbelow selects Base.*)
  - 
        : cflow(call(public void Target.run()) && !within(Base+))
          (cflowbelow selects Base.*)
  - 
        : cflow(call(public void Target.run()))
        && !this(Base+)
          (some join points in Base/Derived do not have Base/Derived as this)
  - 
        : cflow(call(public void Target.run()))
        && !target(Base+)
          (some join points in Base/Derived do not have Base/Derived as target)
*/
