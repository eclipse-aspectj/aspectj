import org.aspectj.testing.Tester; 

public class PointcutQualification {
    public static void main(String[] args) {
        Tester.expectEvent("before pc_reference");
        new TargetClass().doit();
        Tester.checkAllEvents();
    }
}

class I {
    public static final void got(String s) {
        Tester.event(s);
    }
}

class TargetClass{ void doit(){}}

aspect DebugAspect {  // incorrect compiler error here
    before() : Aspect.pc_reference() { I.got("before pc_reference");}
}

aspect Aspect { 

    pointcut pc_notfound() 
        : execution(void TargetClass.doit()) ;

    pointcut someCallCflow() 
        : !within(Aspect)  && !within(DebugAspect) && !within(I)
        //&& cflow(Aspect.pc_notfound()) ;  // workaround
    && cflow(pc_notfound()) ; // bug: unqualified reference in DebugAspect context

    pointcut pc_reference() : someCallCflow();
}


