import org.aspectj.testing.*;
public class ExtendsOuterAbstract_PR408 extends OuterAbstract_PR408 {
    public void go() {
        InnerAbstract inner = new InnerAbstract("string") {};
        inner.go();
        Tester.event("ExtendsOuterAbstract_PR408.go");
    }
}
