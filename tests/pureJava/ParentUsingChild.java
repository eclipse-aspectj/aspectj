
import org.aspectj.testing.*;

/** @testcase PUREJAVA PR#728 interface using preceding subinterface in its definition (order matters) */
interface Child extends Parent {
    interface Toy { }
}

interface Parent { // order matters - must be after Child
    Child.Toy battle();
}

public class ParentUsingChild {
    public static void main (String[] args) {
        Tester.check(Parent.class.isAssignableFrom(Child.class),
                     "!Parent.class.isAssignableFrom(Child.class)");
        Parent p = new Parent() {
                public Child.Toy battle() {
                    return new Child.Toy(){};
                }
            };
        Child.Toy battle = p.battle();
        Tester.check(battle instanceof Child.Toy,
                     "!battle instanceof Child.Toy");
    } 
}
