public class AmbiguousInterface {
    public static void main(String[] args) {
        org.aspectj.testing.Tester.check(false, "shouldn't have compiled");
    }
}
interface Outer {
    interface Inner extends Outer {
        interface Questionable {}
    }
    interface Questionable extends Inner {}
}
interface Another extends Outer, Outer.Questionable {
    interface AnotherInner extends Questionable {}
}
