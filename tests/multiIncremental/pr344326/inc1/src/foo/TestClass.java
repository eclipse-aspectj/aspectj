package foo;

public class TestClass { 

    public boolean callInner() {
        return new TestClassWithInner.InnerTest().getTest();
    }
}
