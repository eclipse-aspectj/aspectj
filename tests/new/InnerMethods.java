import org.aspectj.testing.Tester;

public class InnerMethods {
    public static void main(String[] args) {
	Tester.checkEqual(new Sub().new SubInner().f(2), 21);

	Tester.check("ran before calls advice");
    }
}

class Super {
    protected int m(int j) { return j*10; }

}

interface I {
    int x = 1;
}

class Sub extends Super implements I {
    protected int m(int i) { return i; }

    class SubInner extends Super {
        int f(int jj) {
	    return this.m(jj) + x;
	}
    }
}

aspect A {
    before (): call(int Super.m(int)) {
	Tester.note("ran before calls advice");
    }
}
