import org.aspectj.testing.Tester;
public class EachCFlowRoot1 {
    public static void main(String[] args) {
        Tester.check(true, "compiled");
    }
}
aspect A  of eachcflow(A.pctop()) {
    pointcut pc() : within(C) ;
    pointcut pctop(): pc() && !cflow(pc());
}
class C {}
