import org.aspectj.testing.Tester; 

public class InstanceOf {
    public static void main(String[] args) {
        Tester.expectEvent("instanceOf");
        new TargetClass().instanceOf();
        Tester.checkAllEvents();
    }
}

aspect InstanceOfAspect {
    /** @testcase Introduced type unavailable to instanceof expressions in introduced methods */
    public void TargetClass.instanceOf() {
        /* expecting compiler error,
           but only getting compiler warnings and generated class?
           -> needs work
        */
        // -------- RuntimeException: "Unsupported emit on NotFoundType" Type.java:460
        if (!((getboolean()) instanceof boolean)) { Util.fail("boolean"); }
        if (!((getchar()) instanceof char))       { Util.fail("char"); }
        if (!((getbyte()) instanceof byte))       { Util.fail("byte"); }
        if (!((getshort()) instanceof short))     { Util.fail("short"); }
        if (!((getint()) instanceof int))         { Util.fail("int"); }
        if (!((getlong()) instanceof long))       { Util.fail("long"); }
        if (!((getfloat()) instanceof float))     { Util.fail("float"); }
        if (!((getdouble()) instanceof double))   { Util.fail("double"); }
        // ------ todo: expecting error, get RuntimeException
        //if (!((doVoid()) instanceof Void)) { Tester.check(false,"void"); }
        Util.signal("instanceOf");
    }
}

