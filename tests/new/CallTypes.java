import org.aspectj.testing.Tester;
import java.util.*;

public class CallTypes {
    public static void main(String[] args) {
	C1 c1 = new C1();

	preTest("c1.foo()");
	c1.foo();
	test("static c, static c1, instanceof c, instanceof c1, ");

	C c = c1;

	preTest("(C c = c1).foo()");
	c.foo();
	test("static c, instanceof c, instanceof c1, ");

	c = new C();

	preTest("new C().foo()");
	c.foo();
	test("static c, instanceof c, ");

	C2 c2 = new C2();

	preTest("new C2().foo()");
	c2.foo();
	test("static c, static c2, instanceof c, instanceof c2, ");

	preTest("c1.foo1()");
        c1.foo1();
	test("", "static c1, instanceof c, instanceof c1, ");
    }

    public static void preTest(String str) {
        A.noteAdvice = A.noteAdviceStar = "";
        msg = str;
    }

    static String msg;
    public static void test(String t1) {
	test(t1, t1);
    }

    public static void test(String baseString, String starString) {
	Tester.checkEqual(sort(A.noteAdvice),     sort(baseString), "base: "+msg);
	Tester.checkEqual(sort(A.noteAdviceStar), sort(starString), "star: "+msg);
    }
    private final static Collection sort(String str) {
        SortedSet sort = new TreeSet();
        for (StringTokenizer t = new StringTokenizer(str, ",", false);
             t.hasMoreTokens();) {
            String s = t.nextToken().trim();
            if (s.length() > 0) sort.add(s);
        }
        return sort;
    }
}

class C {
    public void foo() { }
}
class C1 extends C {
    public void foo1() { }
}
class C2 extends C {
    public void foo() { }
}

aspect A {
    static String noteAdvice = "";
    static String noteAdviceStar = "";

    before(C c): target(c) && call(void C.foo()) {
	noteAdvice += "static c, ";
    }
    before(C1 c1): target(c1) && call(void C1.foo()) {
	noteAdvice += "static c1, ";
    }
    before(C2 c2): target(c2) && call(void C2.foo()) {
	noteAdvice += "static c2, ";
    }


    before(C c): target(c) && call(void foo()) {
	noteAdvice += "instanceof c, ";
    }
    before(C1 c1): target(c1) && call(void foo()) {
	noteAdvice += "instanceof c1, ";
    }
    before(C2 c2): target(c2) && call(void foo()) {
	noteAdvice += "instanceof c2, ";
    }


    before(C c): target(c) && call(void C.foo*()) {
	noteAdviceStar += "static c, ";
    }
    before(C1 c1): target(c1) && call(void C1.foo*()) {
	noteAdviceStar += "static c1, ";
    }
    before(C2 c2): target(c2) && call(void C2.foo*()) {
	noteAdviceStar += "static c2, ";
    }


    before(C c): target(c) && call(void foo*()) {
	noteAdviceStar += "instanceof c, ";
    }
    before(C1 c1): target(c1) && call(void foo*()) {
	noteAdviceStar += "instanceof c1, ";
    }
    before(C2 c2): target(c2) && call(void foo*()) {
	noteAdviceStar += "instanceof c2, ";
    }
}
