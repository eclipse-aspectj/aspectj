
import org.aspectj.testing.Tester;

/** @testcase PR#657 PUREJAVA assert statement in advice coverage [requires 1.4] */
public class AssertInAdvice { 
    public static void main(String[] args) {
        AssertInAdvice.class.getClassLoader().setClassAssertionStatus("Test", true);        
        AssertInAdvice.class.getClassLoader().setClassAssertionStatus("AssertInAdvice", false);        

        boolean expectAssert = false;
        boolean gotit = false;
        do {
            Test.throwAssert = expectAssert;
            gotit = false;
            // 6 cases - separate join point for advice below
            // call
            try { call1(); } catch (AssertionError e) { gotit = true; }
            Tester.check(gotit == expectAssert, "call1"+expectAssert); gotit = false;
            try { call2(); } catch (AssertionError e) { gotit = true; }
            Tester.check(gotit == expectAssert, "call2"+expectAssert); gotit = false;
            try { call3(); } catch (AssertionError e) { gotit = true; }
            Tester.check(gotit == expectAssert, "call3"+expectAssert); gotit = false;
            try { call4(); } catch (AssertionError e) { gotit = true; }
            Tester.check(gotit == expectAssert, "call4"+expectAssert); gotit = false;
            try { call5(); } catch (AssertionError e) { gotit = true; }
            Tester.check(gotit == expectAssert, "call5"+expectAssert); gotit = false;
            try { call6(); } catch (AssertionError e) { gotit = true; }
            Tester.check(gotit == expectAssert, "call6"+expectAssert); gotit = false;
            // execution 
            try { execution1(); } catch (AssertionError e) { gotit = true; }
            Tester.check(gotit == expectAssert, "execution1"+expectAssert); gotit = false;
            try { execution2(); } catch (AssertionError e) { gotit = true; }
            Tester.check(gotit == expectAssert, "execution2"+expectAssert); gotit = false;
            try { execution3(); } catch (AssertionError e) { gotit = true; }
            Tester.check(gotit == expectAssert, "execution3"+expectAssert); gotit = false;
            try { execution4(); } catch (AssertionError e) { gotit = true; }
            Tester.check(gotit == expectAssert, "execution4"+expectAssert); gotit = false;
            try { execution5(); } catch (AssertionError e) { gotit = true; }
            Tester.check(gotit == expectAssert, "execution5"+expectAssert); gotit = false;
            try { execution6(); } catch (AssertionError e) { gotit = true; }
            Tester.check(gotit == expectAssert, "execution6"+expectAssert);
            
            // run for false, true
            if (expectAssert) break;
            expectAssert = true;
        } while (true);
    }
    public static void call1() {}
    public static void call2() {}
    public static void call3() {}
    public static void call4() {}
    public static void call5() {}
    public static void call6() {}
    public static void execution1() {}
    public static void execution2() {}
    public static void execution3() {}
    public static void execution4() {}
    public static void execution5() {}
    public static void execution6() {}
}

aspect Test {
    // todo: convert to 1.3 test?
    public static boolean throwAssert; 
    public static boolean throwAssert() { return throwAssert; }
    public static final AssertionError EXPECTED = new AssertionError("expected");
    public static void testAssert() {
        //if (throwAssert) throw EXPECTED; 
        assert !throwAssert() ;
    }
    // call
    after() returning : call(void AssertInAdvice.call1() ) { 
      assert !throwAssert();
    }
    after() : call(void AssertInAdvice.call2() ) { 
      assert !throwAssert();
     }
    before() : call(void AssertInAdvice.call3() ) { testAssert(); }
    void around() : call(void AssertInAdvice.call4() ) { testAssert(); }
    void around() : call(void AssertInAdvice.call5() ) { proceed(); testAssert(); }
    void around() : call(void AssertInAdvice.call6() ) { testAssert(); proceed(); }

    // execution
    after() returning : execution(void AssertInAdvice.execution1() ) { testAssert(); }
    after() : execution(void AssertInAdvice.execution2() ) { testAssert(); }
    before() : execution(void AssertInAdvice.execution3() ) { testAssert(); }
    void around() : execution(void AssertInAdvice.execution4() ) { testAssert(); }
    void around() : execution(void AssertInAdvice.execution5() ) { proceed(); testAssert(); }
    void around() : execution(void AssertInAdvice.execution6() ) { testAssert(); proceed(); }
}
