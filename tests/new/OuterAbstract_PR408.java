import org.aspectj.testing.*;

public abstract class OuterAbstract_PR408 {
    public void go() {
        Tester.event("OuterAbstract_PR408.go");
    }
    public abstract class InnerAbstract {
        public InnerAbstract(String str) {}
        public void go() {
            Tester.event("InnerAbstract.go");
        }
    }
}
