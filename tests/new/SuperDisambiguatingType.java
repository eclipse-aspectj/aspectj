import org.aspectj.testing.*;


/** @testcase PUREJAVA super reference used to disambiguate names of different but compatible types */
public class SuperDisambiguatingType {
    public static void main (String[] args) {
        new Sub().test();
        Tester.checkAllEvents();
    } 
    static {
        Tester.expectEvent("test");
    }
}


interface I { }
class C { }
class IClass implements I { }
class CClass extends C { }

class Super {
    protected C fieldC;
    protected I fieldI;
}

class Sub extends Super {
    // hiding super
    protected CClass fieldC;
    protected IClass fieldI;
    protected Integer intField;
    public void test() {
        testC();
        testI();
        Tester.event("test");
    }
    public void testC() {
        super.fieldC = (C) (fieldC = new CClass());
        checkC("super.fieldC = (C) (fieldC = new CClass())");
        super.fieldC = (C) fieldC;
        checkC("super.fieldC = (C) fieldC");
        super.fieldC = fieldC;
        checkC("super.fieldC = fieldC");
    }
    private final void checkC(String label) {
        Tester.check(null != fieldC, label + "null != fieldC");
        Tester.check(super.fieldC == fieldC, label + "super.fieldC == fieldC");
    }
    public void testI() {
        super.fieldI = (I) (fieldI = new IClass());
        checkI("super.fieldI = (I) (fieldI = new IClass())");
        super.fieldI = (I) fieldI;
        checkI("super.fieldI = (I) fieldI");
        super.fieldI = fieldI;
        checkI("super.fieldI = fieldI");
    }
    private final void checkI(String label) {
        Tester.check(null != fieldI, label + "null != fieldI");
        Tester.check(super.fieldI == fieldI, label + "super.fieldI == fieldI");
    }
}

