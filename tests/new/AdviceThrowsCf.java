import org.aspectj.testing.Tester;
import java.io.IOException;

public class AdviceThrowsCf {
    public static void main(String[] args) {
        try {
            new C().m1();
            Tester.checkFailed("m1");
        } catch (CheckedExc ce) {
            Tester.checkEqual("b1", ce.getMessage(), "m1");
        }
        try {
            new C().m2();
            Tester.checkFailed("m2");
        } catch (UncheckedExc ce) {
            Tester.checkEqual("b3", ce.getMessage(), "m2");
        }
        try {
            new C().m3();
            Tester.checkFailed("m3");
        } catch (CheckedExc ce) {
            Tester.checkEqual("b1", ce.getMessage(), "m3");
        } catch (Exception e) {
            Tester.checkFailed("IOException");
            System.out.println("m3: " + e);
        }
        try {
            new C().m4();
            Tester.checkFailed("m4");
        } catch (UncheckedExc ce) {
            Tester.checkEqual("b3", ce.getMessage(), "m4");
        }
    }

    
}

class CheckedExc extends Exception {
    CheckedExc(String m) { super(m); }
}

class UncheckedExc extends RuntimeException {
    UncheckedExc(String m) { super(m); }
}


class C {
    int x=0;
    public void m1() throws CheckedExc {
        x += 1;
    }

    public void m2() throws UncheckedExc {
    }

    public void m3() throws IOException, CheckedExc {
    }

    public void m4() {
    }
}

aspect A {
    pointcut canThrowChecked(): call(* C.m1()) || call(* C.m3());
    pointcut canThrowChecked1(): call(* C.m*() throws CheckedExc);

    pointcut canThrowUnchecked(): call(* C.m*());


    before() throws CheckedExc: canThrowUnchecked() {  // ERR: m2 and m4
        throw new CheckedExc("b1");
    }

    before() throws CheckedExc: get(int C.x) { //ERR: all gets
    }
    before() throws CheckedExc: set(int C.x) { //ERR: all sets
    }
    before() throws CheckedExc: staticinitialization(C) { //ERR: can't throw
    }
    
    void around() throws CheckedExc: canThrowChecked() {
    	proceed();
    }
    
    void around() throws CheckedExc: canThrowUnchecked() {  // ERR: can't throw
    	proceed();
    }
    
    void around() throws UncheckedExc: canThrowUnchecked() || set(int C.x) { 
    	proceed();
    }

}
