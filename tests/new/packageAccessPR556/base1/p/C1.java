package p;

import p.*;
import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

public aspect C1 {
    private static int privateOne = 1 ; //C2.privateOne
    /** @testcase PR#556 aspects get package access to variables in other aspects */
    /** @testcase PR#556 aspects get package access to variables in other classes */
    public static void main(String[] args) {
        int i = C2.defaultOne + C2.protectedOne + C2.publicOne
              + A2.defaultOne + A2.protectedOne + A2.publicOne;
        Tester.expectEvent("C2");
        Tester.expectEvent("A2");
        Tester.check(i==6, "initialization failed: " + i);
        try { System.getProperty("ignore" + i); } 
        catch (Exception e) {} 
        Tester.checkAllEvents();
    }
    /** @testcase class pointcuts visible via package-access from another aspect */
    before () : C2.p() && execution(static void C1.main(String[])) {
        Tester.event("C2");
    }
    /** @testcase aspect pointcuts visible via package-access from another aspect */
    before () : A2.p() && execution(static void C1.main(String[])) {
        Tester.event("A2");
    }
}
