package a;

import org.aspectj.testing.Tester; 

/** @testcase PR#626 declared parent not defined in scope of target class declaration (CE in -usejavac only) */
public abstract aspect Outer {
    public static void main (String[] args) {
        b.Foo foo = new b.Foo() {
                public void run() {
                    Tester.event("run");
                }
            };
        Inner i = (Inner) foo;
        i.run();
        Tester.checkAllEvents();
    } 

    static {
        Tester.expectEvent("run");
    }

    protected interface Inner {
        public void run();
    }

}

