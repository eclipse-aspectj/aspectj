
import org.aspectj.testing.Tester;

/** @testcase PR#33948 default constructor inter-type declaration */
public class ConstructorDeclaration {
    public static void main(String[] args) {
        Tester.expectEvent("create");
        new ConstructorDeclaration();
        Tester.checkAllEvents();
    }
}

aspect A {
    ConstructorDeclaration.new() {
        Tester.event("create");
    }
}