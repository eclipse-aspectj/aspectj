
public class MethodsNotFound {
    public void foo() {
	"a".bar();
    }

    public void foo(int x) { 
	new Ha().foo();
    }

    public void bar() { 
	new MethodsNotFound().foo("hi");
    }
}

class Ha {
    private void foo() {}
}
