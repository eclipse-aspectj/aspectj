import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

public class ThisInIntroFieldInit {
    public static void main(String[] args) {
        Tester.expectEvent("result init");
        Tester.expectEvent("internalResult init");
        TargetClass me = new TargetClass();

        // pure-java - field references
        Tester.check(me.t_result_ref, "me.t_result_ref");
        Tester.check(me.t_result_ref_this, "me.t_result_ref_this");
        Tester.check(me.t_result_ref_qualified, "me.t_result_ref_qualified");

        // pure-java - method references
        Tester.check(me.t_result, "me.t_result");
        Tester.check(me.t_result_this, "me.t_result_this");
        Tester.check(me.t_result_qualified, "me.t_result_qualified");
        Tester.check(me.t_result_anon, "me.t_result_anon");

        // introduction - field references
        // local initializers are run after introduced initializers in 1.1
        Tester.check(!me.result_ref, "me.result_ref");
        Tester.check(!me.result_ref_this, "me.result_ref_this");
        Tester.check(!me.result_ref_qualified, "me.result_ref_qualified");

        // introduction - method references
        Tester.check(me.result, "me.result");
        Tester.check(me.result_this, "me.result_this");
        Tester.check(me.result_qualified, "me.result_qualified");
        Tester.check(me.result_anon, "me.result_anon");

        Tester.checkAllEvents();
    }
}

class TargetClass {
    boolean targTest() { return (null != this); }
    public boolean ok = (this != null);
    /** @testcase field reference in field initializer */
    public boolean t_result_ref = ok;
    /** @testcase this field reference in field initializer */
    public boolean t_result_ref_this = this.ok;
    /** @testcase qualified this field reference in field initializer */
    public boolean t_result_ref_qualified = TargetClass.this.ok;
    /** @testcase method reference in field initializer */
    public boolean t_result = targTest();
    /** @testcase this method in field initializer */
    public boolean t_result_this = this.targTest();
    /** @testcase qualified this method in field initializer */
    public boolean t_result_qualified = TargetClass.this.targTest();
    /** @testcase qualified this method in field initializer anon class definition */
    boolean t_result_anon 
        = new Validator() {
                public boolean valid() {
                    boolean one = targTest();
                    boolean two = TargetClass.this.targTest();
                    boolean three = ok;
                    boolean four = TargetClass.this.ok;
                    Tester.event("internalResult init");
                    return (one && two && three && four);
                }}.valid();
}
interface Validator {
    public boolean valid();
}

aspect A {
    /** @testcase field reference in field initializer */
    public boolean TargetClass.result_ref = ok;
    /** @testcase this field reference in field initializer */
    public boolean TargetClass.result_ref_this = this.ok;
    /** @testcase qualified this field reference in field initializer */
    public boolean TargetClass.result_ref_qualified = TargetClass.this.ok;

    /** @testcase method reference in introduced field initializer */
    public boolean TargetClass.result = targTest();
    /** @testcase this method in introduced field initializer */
    public boolean TargetClass.result_this = this.targTest();
    /** @testcase qualified this method in introduced field initializer */
    public boolean TargetClass.result_qualified = TargetClass.this.targTest();
    /** @testcase qualified this method in introduced field initializer anon class definition */
    public boolean TargetClass.result_anon
        = new Validator() {
                public boolean valid() {
                    boolean one = targTest();
                    boolean two = TargetClass.this.targTest();
                    boolean three = !ok;
                    boolean four = !TargetClass.this.ok;
                    Tester.event("result init");
                    return (one && two && three && four);
                }}.valid();
}
