
package pack;

import org.aspectj.testing.Tester;

public aspect DefineInterface {
    declare parents: InterfaceDefinition.C implements MyInterface;
    static {
        Tester.expectEvent("m()");
    }
    public void MyInterface.m() {
        Tester.event("m()");
    }
    before(MyInterface targ) : target(targ) && call(void run()) {
        targ.m();
    }
    after() returning : execution(void main(String[])) {
        Tester.checkAllEvents();
    }
}
