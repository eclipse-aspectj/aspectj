import org.aspectj.testing.Tester;

public class ExpandedDotDotPattern {
    public static void main(String[] args) {
	new A().foo();
	new B().foo(3);
	new C().foo(3, 3);
	new D().foo(3, 3, 3);
	new E().foo(3, 3, 3, 3);
	new F().foo(3, 3, 3, 3, 3);
	Tester.checkEqual(A.count, 1 + 1 + 1 + 1 + 1, "not enough 0-ary");
	Tester.checkEqual(B.count, 0 + 2 + 3 + 4 + 5, "not enough 1-ary");
	Tester.checkEqual(C.count, 0 + 1 + 4 + 7 + 11, "not enough 2-ary");
	Tester.checkEqual(D.count, 0 + 1 + 3 + 8 + 15, "not enough 3-ary");
	Tester.checkEqual(E.count, 0 + 1 + 3 + 7 + 16, "not enough 4-ary");
	Tester.checkEqual(F.count, 0 + 1 + 3 + 7 + 15, "not enough 5-ary");
    }
}

interface I {
    void inc();
}

class A implements I {
    static int count = 0;
    public void inc() { count++; }
    void foo() {}
}
class B implements I {
    static int count = 0;
    public void inc() { count++; }
    void foo(int a) {}
}
class C implements I {
    static int count = 0;
    public void inc() { count++; }
    void foo(int a, int b) {}
}
class D implements I {
    static int count = 0;
    public void inc() { count++; }
    void foo(int a, int b, int c) {}
}
class E implements I {
    static int count = 0;
    public void inc() { count++; }
    void foo(int a, int b, int c, int d) {}
}
class F implements I {
    static int count = 0;
    public void inc() { count++; }
    void foo(int a, int b, int c, int d, int e) {}
}    


aspect Aspect {
    // zero
    before(I i): this(i) && execution(void foo()) {
	System.out.println(thisJoinPoint); 
        i.inc();
    }

    // one
    before(I i): this(i) && execution(void foo( ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo(int)) {
	System.out.println("(int)" + thisJoinPoint); 
	i.inc();
    }

    // two
    before(I i): this(i) && execution(void foo( ..,  ..)) { 
	System.out.println("(.., ..)" + thisJoinPoint); 
	i.inc(); 
    }

    before(I i): this(i) && execution(void foo(int,  ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo( .., int)) { 
	System.out.println("(.., int)" + thisJoinPoint); 
	i.inc();
    }

    before(I i): this(i) && execution(void foo(int, int)) { i.inc(); }

    // three
    before(I i): this(i) && execution(void foo( ..,  ..,  ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo( ..,  .., int)) { i.inc(); }

    before(I i): this(i) && execution(void foo( .., int,  ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo( .., int, int)) { i.inc(); }

    before(I i): this(i) && execution(void foo(int,  ..,  ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo(int,  .., int)) { i.inc(); }

    before(I i): this(i) && execution(void foo(int, int,  ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo(int, int, int)) { i.inc(); }

    // four

    before(I i): this(i) && execution(void foo( ..,  ..,  ..,  ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo( ..,  ..,  .., int)) { i.inc(); }

    before(I i): this(i) && execution(void foo( ..,  .., int,  ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo( ..,  .., int, int)) { i.inc(); }

    before(I i): this(i) && execution(void foo( .., int,  ..,  ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo( .., int,  .., int)) { i.inc(); }

    before(I i): this(i) && execution(void foo( .., int, int,  ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo( .., int, int, int)) { i.inc(); }


    before(I i): this(i) && execution(void foo(int,  ..,  ..,  ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo(int,  ..,  .., int)) { i.inc(); }

    before(I i): this(i) && execution(void foo(int,  .., int,  ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo(int,  .., int, int)) { i.inc(); }

    before(I i): this(i) && execution(void foo(int, int,  ..,  ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo(int, int,  .., int)) { i.inc(); }

    before(I i): this(i) && execution(void foo(int, int, int,  ..)) { i.inc(); }

    before(I i): this(i) && execution(void foo(int, int, int, int)) { i.inc(); }

}
