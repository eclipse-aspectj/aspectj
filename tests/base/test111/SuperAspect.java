import org.aspectj.testing.Tester;

public aspect SuperAspect {
   before(SuperClass s): target(s) && call(* getX(..)) {
      Tester.checkEqual(thisJoinPoint.getSignature().getName(), "getX", "method name");
      //!!! It's unclear what they value of this really should be
      //Tester.checkEqual(thisJoinPoint.className, "SubClass", "class name");
      // make a change to prove this is run
      //x = x+42;
      s.setX( s.checkX()+42 );
    }
}
