import org.aspectj.testing.Tester;

class NonOverEagerConstantFolding {
    public static final int i = 3;
    static boolean thingy = false;
    static NonOverEagerConstantFolding foo() {
	thingy = true;
	return null;
    }
    public static void main(String[] args) {
	int j = 3 + foo().i;
	Tester.check(thingy, "didn't evaluate expr part of field access expr");
    }
}
/*
class Test {
    int i = 3;
    class C {
	C() {
	    ++j;
	}
	int j = i + 2;
    }
    void foo() {
	new C();
    }
}
*/
