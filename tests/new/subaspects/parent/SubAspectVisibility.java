
package parent;

import child.ForeignChildAspect;

import org.aspectj.testing.*;

/** @testcase PR#647 inner, outer, and outside-package subaspects of an aspect with abstract protected-, public-, and default-access pointcuts */
public abstract aspect SubAspectVisibility {
    public static void main (String[] args) {
        Tester.event("main");
        OuterChild.main(args);
        ForeignChildAspect.main(args);
        Tester.checkAllEventsIgnoreDups();
    } 
    static {
        Tester.expectEvent("main");
        Tester.expectEvent("definePrivate");
        Tester.expectEvent("definePackagePrivate");
        Tester.expectEvent("defineProtected");
        Tester.expectEvent("definePublic");

        Tester.expectEvent("Outer.main");
        Tester.expectEvent("Outer.definePackagePrivate");
        Tester.expectEvent("Outer.defineProtected");
        Tester.expectEvent("Outer.definePublic");

        Tester.expectEvent("ForeignChildAspect.main");
        Tester.expectEvent("ForeignChildHelper.definePackagePrivate");
        Tester.expectEvent("ForeignChildAspect.defineProtected");
        Tester.expectEvent("ForeignChildAspect.definePublic");
    }
    
    before() : definePrivate() {
        Tester.event("definePrivate");
    }

    before() : definePackagePrivate() {
        Tester.event("definePackagePrivate");
    }

    before() : defineProtected() {
        Tester.event("defineProtected");
    }

    before() : definePublic() {
        Tester.event("definePublic");
    }

    /** public can be implemented in outer or inner child class */
    abstract public pointcut definePublic();  

    /** protected can be can be implemented in outer or inner child class */
    abstract protected pointcut defineProtected();  

    /** package-private can be can be implemented in outer or inner child class */
    abstract pointcut definePackagePrivate();  

    /** private must be implemented in defining class */
    private pointcut definePrivate() : execution(void SubAspectVisibility.main(..));

    // bug? says definePrivate() is not defined in InnerChild 
    static aspect InnerChild extends SubAspectVisibility {
        /** @testCase override protected pointcut in inner class */
        protected pointcut defineProtected() : execution(void SubAspectVisibility.main(..));
        /** @testCase override package-private pointcut in inner class */
        pointcut definePackagePrivate() : execution(void SubAspectVisibility.main(..));
        /** @testCase override public pointcut in inner class */
        public pointcut definePublic() : execution(void SubAspectVisibility.main(..));
    }
}

aspect OuterChild extends SubAspectVisibility {
    public static void main (String[] args) {
        Tester.event("Outer.main");
    } 
    
    /** @testCase override package-private pointcut in outer class */
    pointcut definePackagePrivate() : execution(void OuterChild.main(..));
    /** @testCase override protected pointcut in outer class */
    protected pointcut defineProtected() : execution(void OuterChild.main(..));
    /** @testCase override public pointcut in outer class */
    public pointcut definePublic() : execution(void OuterChild.main(..));

    before() : definePackagePrivate() {
        Tester.event("Outer.definePackagePrivate");
    }
    before() : defineProtected() {
        Tester.event("Outer.defineProtected");
    }
    before() : definePublic() {
        Tester.event("Outer.definePublic");
    }
}

