import org.aspectj.testing.Tester; 

public class Inner {
    public static void main(String[] args) {
        Tester.expectEvent("inner");
        new TargetClass().inner();
        Tester.checkAllEvents();
    }
}

aspect InstanceOfAspect {
    /** @testcase  Introduced type unavailable to qualified new expressions in introduced methods */
    public void TargetClass.inner() {
        InnerClass i = this.new InnerClass();
        if (!i.valid()) Util.fail("this.new InnerClass()");
        InnerClass j = getThis().new InnerClass();
        if (!j.valid()) Util.fail("getThis().new InnerClass()");
        Util.signal("inner");
    }
}

