public class ProtectedIntro {
    public static void main(String[] args) {
        new ProtectedIntro().realMain(args);
    }
    public void realMain(String[] args) {
        org.aspectj.testing.Tester.check(false, "shouldn't have compiled");
    }
    
    public ProtectedIntro() {
    }
}
class C {}
aspect A {
    protected void C.foo() {}; // can't do this
}
