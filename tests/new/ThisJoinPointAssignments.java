public class ThisJoinPointAssignments {
    public static void main(String[] args) {
        new ThisJoinPointAssignments().realMain(args);
    }
    public void realMain(String[] args) {
        new C().f();
        org.aspectj.testing.Tester.check(A.jp != null, "Didn't set the JoinPoint");
    }
}

class C {
    public void f() {}
}

aspect A {
    static org.aspectj.lang.JoinPoint jp;
    before(): call(void C.f()) {
        jp = thisJoinPoint;
    }
}
