import org.aspectj.testing.*;
public class ConstructorNotFound_PR408 {
    public static void main(String[] args) {
        new ExtendsOuterAbstract_PR408().go();
        new OuterAbstract_PR408() {}.go();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEventsInString("ExtendsOuterAbstract_PR408.go,InnerAbstract.go,OuterAbstract_PR408.go");
    }
}
