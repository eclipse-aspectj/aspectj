public class ProtectedFieldIntro {
    public static void main(String[] args) {
        new ProtectedFieldIntro().realMain(args);
    }
    public void realMain(String[] args) {
        org.aspectj.testing.Tester.check(false, "shouldn't have compiled");
    }
    
    public ProtectedFieldIntro() {
    }
}
class C {}
aspect A {
    protected int C.i = 13; // can't do this
}
