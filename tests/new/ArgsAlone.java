import org.aspectj.testing.Tester; 

public class ArgsAlone {
    public static void main(String[] args) {
        Tester.expectEvent("within 2 method-call");
        Tester.expectEvent("within 2 method-execution");
        new TargetClass().callInt(2);
        Tester.checkAllEvents();
    }
}

class TargetClass {
    void callInt(int i) {
        while (i > 0) { --i; }
    }

}

aspect Aspect {
    pointcut pc () 
        : (call(void TargetClass.callInt(int))
           || execution(void TargetClass.callInt(int)));

    before(int i) 
        : !target(Aspect) && args(i)  && !target(StringBuffer)
        //&& pc() // uncomment to avoid InternalCompilerError
        {
            Tester.event("within " + i 
                             + " " + thisJoinPointStaticPart.getKind());
        }
}
