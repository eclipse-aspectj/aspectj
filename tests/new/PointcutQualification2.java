import org.aspectj.testing.Tester; 

public class PointcutQualification2 {
    public static void main(String[] args) {
        Tester.expectEvent("before pc_reference2");
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

aspect DebugAspect2 {  // incorrect compiler error here
  before() : Aspect2.pc_reference2() { I.got("before pc_reference2");}
}

aspect Aspect2 { 

    pointcut pc_notfound2() 
        : execution(void TargetClass.doit()) ;

    //pointcut anotherRef()   : Aspect2.pc_notfound2();  // workaround
    pointcut anotherRef()     : pc_notfound2(); 

    pointcut pc_reference2()  : anotherRef();
}

