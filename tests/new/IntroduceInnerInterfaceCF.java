
import org.aspectj.testing.Tester; 
import org.aspectj.testing.Tester;

public class IntroduceInnerInterfaceCF {
    public static void main(String[] args) {
        Tester.checkFailed("!compile");
    }
    private static final String[] EXPECTED;
    static {
        EXPECTED = new String[] 
        { "walk", "execution(void TargetClass.walk())" };
        Tester.expectEventsInString(EXPECTED);
    }
}
class TargetClass {
    /** @testcase PR#494 compile should fail if implementing method is not public */
    void defaultMethod() { }               // errLine 18
    private void privateMethod() { }       // errLine 19
    protected void protectedMethod() { }   // errLine 20
}

class AnotherClass{}  
class AThirdClass extends TargetClass implements Aspect.Inner {} // errLine 24

aspect Aspect {
    private interface Inner {
        // all automagically interpreted as public
        void defaultMethod();
        void privateMethod();
        void protectedMethod();
    }
    declare parents
        : TargetClass implements Inner;
    before() : execution(void Inner.*()) {
    }
}

aspect PeekingAspect {
    after(TargetClass tc) : this(tc) && execution(void TargetClass.walk()) {
        /** @testcase PR#494 compile should fail to bind private interface name outside of Aspect */
        if (tc instanceof Aspect.Inner) {                         // errLine 42
            Tester.checkFailed("(tc instanceof Aspect.Inner)");
        }
        if (Aspect.Inner.class.isAssignableFrom(tc.getClass())) { // errLine 45
            Tester.checkFailed("(Aspect.Inner.class.isAssignableFrom(tc.getClass())");
        }
        ((Aspect.Inner) tc).defaultMethod();                      // errLine 48
    }
    declare parents : AnotherClass implements Aspect.Inner;       // errLine 50
}

abstract aspect AbstractAspect {
    private interface Private {}
}
aspect HideFromChild extends AbstractAspect {
    /** @testcase PR#494 compile should fail to bind private interface name in aspect subclass */
    declare parents : AnotherClass implements Private;              // errLine 58
}

// todo: test cases to validate inner interfaces with package and protected 
