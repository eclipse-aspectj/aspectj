
import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

public class AspectOf {
    public static void main(String[] args) {
        Tester.expectEvent("IsSingleton before AspectOf.run()");
        Tester.expectEvent("PerCFlow before AspectOf.run()");
        Tester.expectEvent("PerTarget before AspectOf.run()");
        Tester.expectEvent("PerThis before AspectOf.run()");
        Tester.expectEvent("PerCFlowBelow before AspectOf.subrun()");
        Tester.expectEvent("run()");
        Tester.expectEvent("subrun()");
        new AspectOf().run();
        Tester.checkAllEvents();
    }
    public void subrun() { 
        Object aspect = PerCFlowBelow.aspectOf();
        Tester.check(null != aspect, "PerCFlowBelow.aspectOf()");
        Tester.event("subrun()");
    }
    public void run() {
        Object aspect = null;
        
        aspect = IsSingleton.aspectOf(); 
        Tester.check(null != aspect, "IsSingleton.aspectOf()");
        aspect = PerThis.aspectOf(this); 
        Tester.check(null != aspect, "PerThis.aspectOf(this)");
        aspect = PerTarget.aspectOf(this);
        Tester.check(null != aspect, "PerTarget.aspectOf(this)");
        aspect = PerCFlow.aspectOf();
        Tester.check(null != aspect, "PerCFlow.aspectOf()");
        Tester.event("run()");
        subrun();
    }
    public static void log(String s) {
        Tester.event(s);
        //System.out.println(s);
    }
}

aspect IsSingleton {
    before() : execution(void AspectOf.run()) {
        AspectOf.log("IsSingleton before AspectOf.run()");
    }
}

aspect PerThis perthis(pc()) {
    pointcut pc() : execution(void AspectOf.run()) ;
    before() : pc() {
        AspectOf.log("PerThis before AspectOf.run()");
    }
}
aspect PerTarget pertarget(pc()) {
    pointcut pc() : execution(void AspectOf.run()) ;
    before() : pc() {
        AspectOf.log("PerTarget before AspectOf.run()");
    }
}

aspect PerCFlow percflow(pc()) {
    pointcut pc() : execution(void AspectOf.run());
    before() : pc() {
        AspectOf.log("PerCFlow before AspectOf.run()");
    }
}

aspect PerCFlowBelow percflowbelow(pc()) {
    pointcut pc() : execution(void AspectOf.run());
    before() : execution(void AspectOf.subrun()) {
        AspectOf.log("PerCFlowBelow before AspectOf.subrun()");
    }
}



