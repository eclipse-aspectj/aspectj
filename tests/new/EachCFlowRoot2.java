import org.aspectj.testing.Tester;
public class EachCFlowRoot2 {
    public static void main(String[] args) {
        Tester.check(true, "compiled");
    }
}
aspect A  of eachcflow(B.pctop()) {
    pointcut pc() : within(C) ;
}
aspect B {
    pointcut pctop(): within(C) && !cflow(within(C));
}
class C {}
