import org.aspectj.testing.Tester;

public class Asserts {
    public static void main(String[] args) {
        Asserts.class.getClassLoader().setClassAssertionStatus("TestAsserts", true);
        TestAsserts.main(args);
    }
}

class TestAsserts {
    public static void main(String[] args) {
        //C c = new C();
        //C.m(9);
        int x = 0;
        assert x < 2;
        assert x <10 : 3;

        boolean pass = false;
        try { assert x > 2; }
        catch (AssertionError e) { pass = true; }
        finally { Tester.check(pass, "no expected assertion-1"); }

        pass = false;
        try { assert x >10 : 3; }
        catch (AssertionError e) { pass = true; }
        finally { Tester.check(pass, "no expected assertion-2"); }
    }

    static class C {
        static void m(int i ) {
            assert i < 10;
        }
    }
}
