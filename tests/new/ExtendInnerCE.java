/** @testcase TestCase PUREJAVA NullPointerException (not compiler error) when extending non-static inner class */
public class ExtendInnerCE { }
class TargetClass Outer.Inner { } // s.b. error: Outer.this. is not accessible

class Outer {
    class Inner {
    }
}
