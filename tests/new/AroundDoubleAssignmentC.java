import org.aspectj.testing.*;

/**
 * with -usejavac: cannot resolve symbol
 * without -usejavac: VerifyError
 */
public aspect AroundDoubleAssignmentC {
    public static void main( String[] args ){
        //---------- passing tests
        // field init
        Tester.expectEvent("proceed-fieldinit");
        new FieldInit();
        
        // field set
        Tester.expectEvent("fieldset");
        Tester.expectEvent("proceed-fieldset");
        new FieldSet().test();


        //---------- failing tests
        // static method, field set
        Tester.expectEvent("staticfieldset-test");
        Tester.expectEvent("proceed-staticset");
        StaticSet.staticTest();

        // static initializer
        Tester.expectEvent("staticinit");
        Tester.expectEvent("proceed-staticinit");
        Class c2 = StaticInit.class.getClass();
        Tester.check("test".equals(StaticInit.string),
                           "\"test\".equals(StaticInit.string)");

        // instance initializer
        Tester.expectEvent("init");
        //XXX see below
        //Tester.expectEvent("proceed-init");
        String s = new Init().string;
        Tester.check("test".equals(s),
                           "\"test\".equals(new Init().string)");
        Tester.checkAllEvents();
    } // main

    Object around() : within(FieldInit) && execution( * *() ) {
        Tester.event("proceed-fieldinit");
        return proceed();
    }

    Object around() : execution( * FieldSet.*() ) {
        Tester.event("proceed-fieldset");
        return proceed();
    }

    // static method
    Object around() : execution( * StaticSet.*() ) {
        Tester.event("proceed-staticset");
        return proceed();
    }

    // static initializer
    Object around() : staticinitialization(StaticInit) {
        Tester.event("proceed-staticinit");
        return proceed();
    }

    // instance initializer
    //XXX not implemented in 1.1
//    Object around() : initialization(Init.new(..)) {
//        Tester.event("proceed-init");
//        return proceed();
//    }
}

class FieldInit {
    /** @testcase PR#687 around all execution with double assignment in initializer (fieldinit) */
    String s = getString();
    { s = s; }
    String getString() { return "test".toString(); }
}

class FieldSet {

    /** @testcase PR#687 around all execution with double assignment in initializer (fieldset) */
    String s;
    public void test(){
        s = s = "test"; // not initializer, so...
        Tester.event("fieldset");
    }
}

class StaticSet {
    /** @testcase PR#687 around all execution with double assignment in initializer (staticfieldset) */
    static String string;
    public static void staticTest(){
        String s = s = "test";
        string = s;
        Tester.event("staticfieldset-" + string);
    }
}

/** @testcase PR#687 around all execution with double assignment in initializer (staticinitialization) */
class StaticInit {
    static String string;
    static {
        String s = s = getString();
        Tester.event("staticinit");
        string = s;
    }
    static String getString() { return "test"; }
}

/** @testcase PR#687 around all execution with double assignment in initializer (instance initialization) */
class Init {
    String string;
    Init() {
        String s = s = "test";
        Tester.event("init");
        string = s;
    }
}

