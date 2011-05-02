package foo;

public class ClassReferencingTestClass {

    public void test() {
        new TestClass().callInner();
    }
}

