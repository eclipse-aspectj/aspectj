import org.aspectj.testing.Tester;

public class TryAndProceed {
    public static void main(String[] args) {
        new C().mNoThrows();
        Tester.checkEqual(C.buf.toString(), "beforeAll:aroundAll:mNoThrows:");
        C.buf = new StringBuffer();

        A.aspectOf().allowThrowingAround = true;
        try {
            new C().mThrowsCheckedExc();
            Tester.checkFailed("should have thrown RuntimeExc");
        } catch (CheckedExc ce) {
            Tester.checkFailed("should have thrown RuntimeExc not " + ce);
        } catch (RuntimeException re) {
            //System.out.println("caught " + re);
        }
        Tester.checkEqual(C.buf.toString(), 
           "beforeAll:aroundCheckedNoThrow:aroundAll:aroundCheckedThrow:aroundCaughtCE:");
        C.buf = new StringBuffer();

        A.aspectOf().allowThrowingBefore = true;
        try {
            new C().mThrowsCheckedExc();
            Tester.checkFailed("should have thrown CheckedExc");
        } catch (CheckedExc ce) {
            //System.out.println("caught " + ce);
        } catch (RuntimeException re) {
            Tester.checkFailed("should have thrown CheckedExc not RuntimeExc");
        }
        Tester.checkEqual(C.buf.toString(), "beforeChecked:");

    }
}


class C {
    public static StringBuffer buf = new StringBuffer();

    public void mThrowsCheckedExc() throws CheckedExc {
        C.buf.append("mThrowsCheckedExc:");
    }

    public void mNoThrows() {
        C.buf.append("mNoThrows:");
    }
}

aspect A {
    pointcut checkedCut(): call(void C.mThrowsCheckedExc());
    pointcut uncheckedCut(): call(void C.mNoThrows());
    pointcut allCut(): checkedCut() || uncheckedCut();

    public static boolean allowThrowingBefore = false;
    public static boolean allowThrowingAround = false;

    before() throws CheckedExc: checkedCut() && if(allowThrowingBefore) {
        C.buf.append("beforeChecked:");
        throw new CheckedExc("from before");
    }

    before(): allCut() {
        C.buf.append("beforeAll:");
    }

    Object around(): checkedCut() {
        C.buf.append("aroundCheckedNoThrow:");
        return proceed();
    }

    Object around(): allCut() {
        C.buf.append("aroundAll:");
        try {
            return proceed();
        } catch (CheckedExc ce) {
            C.buf.append("aroundCaughtCE:");

            throw new RuntimeException("hand-softening CheckedExc");
        }
    }

    Object around() throws CheckedExc: checkedCut() && if(allowThrowingAround) {
        C.buf.append("aroundCheckedThrow:");
        throw new CheckedExc("from around");
    }
}

class CheckedExc extends Exception {
    public CheckedExc(String m) { super(m); }
}
