
import org.aspectj.testing.Tester; 
import org.aspectj.testing.Tester;

public class IntroduceInnerInterfaceCP {
    public static void main(String[] args) {
        new TargetClass().walk();
        new TargetClassWithoutImplementation().run();
        AnotherClass.invoke();
        Tester.checkAllEvents();
    }
    private static final String[] EXPECTED;
    static {
        EXPECTED = new String[] 
        { "walk", "Implementation.walk", 
          "execution(void TargetClass.walk())",
          "AnotherClass.invoke()", 
          "Protected execution(void AnotherClass.invoke())" 
        };
        Tester.expectEventsInString(EXPECTED);
    }
}
class Signal {
    public static final void signal(String s) {
        //System.err.println(s);
        Tester.event(s);
    }
}
class TargetClass {
    public void walk() { 
        Signal.signal("walk");
    }
}

class TargetClassWithoutImplementation {
    void run() { }
}

aspect Aspect {
    /** @testcase PR#494 adding private interface inside aspect */
    private interface Inner {
        // automagically interpreted as public
        void walk();
    }
    /** @testcase PR#494 using private interface inside aspect for introductions */
    declare parents
        : TargetClass implements Inner;
    declare parents
        : TargetClassWithoutImplementation implements Inner;
    declare parents
        : TargetClassWithoutImplementation extends Implementation;
    static class Implementation {
        public void walk() { 
            Signal.signal("Implementation.walk");
        }

    }
    /** @testcase PR#494 using private interface inside aspect in advice */
    before(TargetClassWithoutImplementation t) : target(t)
        && execution(void TargetClassWithoutImplementation.run()) {
        ((Inner) t).walk();
    }

    /** @testcase PR#494 using private interface inside aspect in execution pcd */
    before() : execution(public void Inner.*()) {
        // validate that interface implemented - addressable in pcd
        Signal.signal(thisJoinPointStaticPart.toString());
    }
}

class AnotherClass {
    static void invoke() {
        Signal.signal("AnotherClass.invoke()");
    }
}
abstract aspect AbstractAspect {
    /** Protected has no join points - validate with ShowToChild before advice */
    protected interface Protected {}
}
aspect ShowToChild extends AbstractAspect {
    /** @testcase PR#494 compile should bind protected interface name in aspect subclass for introduction */
    declare parents : AnotherClass implements Protected;              
    /** @testcase PR#494 compile should bind protected interface name in aspect subclass for advice (even when binding static, non-interface methods with tag interfaces)  */
    after () : within(Protected+) && execution(* *(..)) {
        Signal.signal("Protected " + thisJoinPointStaticPart.toString());
    }
    /** Protected has no join points */
    before () : within(Protected) {
        Tester.checkFailed("within Protected");
    }
}

