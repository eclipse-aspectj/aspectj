import org.aspectj.testing.Tester;
public class EachCFlowRoot0 {
    public static void main(String[] args) {
        Tester.check(true, "compiled");
    }
}
aspect A  of eachcflow(A.pc() && !cflow(A.pc())) {
    pointcut pc() : within(C) ;
}
class C {}
