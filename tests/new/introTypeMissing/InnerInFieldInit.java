import org.aspectj.testing.Tester; 
import org.aspectj.testing.Tester;

public class InnerInFieldInit {
    public static void main(String[] args) {
        Tester.expectEvent("innerfield");
        TargetClass me = new TargetClass();
        Tester.check(me.result, "me.result");
        Tester.checkAllEvents();
    }
}

interface Valid  { public boolean valid();}

aspect InnerInFieldInitAspect {
    /** @testcase  Introduced type unavailable to qualified new expressions in introduced field initializers */
    public boolean TargetClass.result =
        new Valid() {
                public boolean valid() {
                    InnerClass i = TargetClass.this.new InnerClass();
                    if (!i.valid()) Util.fail("this.new InnerClass()");
                    InnerClass j = getThis().new InnerClass();
                    if (!j.valid()) Util.fail("getThis().new InnerClass()");
                    Util.signal("innerfield");
                    return i.valid() && j.valid();
                }
            }.valid();
}

